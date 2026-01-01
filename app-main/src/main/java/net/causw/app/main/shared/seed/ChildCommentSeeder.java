package net.causw.app.main.shared.seed;

import net.causw.app.main.shared.util.UserSegmenter;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@Profile("seed")
public class ChildCommentSeeder extends BasePostSeeder<ChildCommentSeeder.ChildCommentItem> {
    private static final Random random = new Random();
    private final List<ParentCommentInfo> parentComments = new ArrayList<>();

    public ChildCommentSeeder(JdbcTemplate jdbcTemplate, UserSegmenter userSegmenter) {
        super(jdbcTemplate, userSegmenter, 50_000, 1_000);
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.COMMENT;
    }

    @Override
    protected void beforeSeeding() {
        loadCommentIds();
    }

    @Override
    protected ChildCommentItem createItem(int index) {
        // 1. 부모 메서드를 통해 확률에 맞는 작성자 선정
        String writerId = pickUser();
        // 2. 낙수 효과에 따라 부모 댓글 선정
        ParentCommentInfo parentInfo = parentComments.get(random.nextInt(parentComments.size()));
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(totalCount - index);

        return new ChildCommentItem(
                UUID.randomUUID().toString(),
                "Child Comment " + index,
                parentInfo.postId(),
                parentInfo.id,
                writerId,
                createdAt
        );
    }

    @Override
    protected void batchInsert(List<ChildCommentItem> items) {
        // 1. 대댓글 삽입
        String sqlChildComment = "INSERT INTO tb_child_comment " +
                "(id, content, parent_comment_id, user_id, is_anonymous, is_deleted, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, false, false, ?, ?)";

        jdbcTemplate.batchUpdate(sqlChildComment, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChildCommentItem item = items.get(i);

                ps.setString(1, item.id());
                ps.setString(2, item.content());
                ps.setString(3, item.parentId());
                ps.setString(4, item.writerId());
                ps.setTimestamp(5, Timestamp.valueOf(item.createdAt()));
                ps.setTimestamp(6, Timestamp.valueOf(item.createdAt()));
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });

        // 2. 알림 삽입
        String sqlNotification = "INSERT INTO tb_notification " +
                "(id, user_id, content, notice_type, target_id, is_global, created_at, updated_at) " +
                "VALUES (?, ?, ?, 'COMMENT', ?, false, ?, ?)";

        jdbcTemplate.batchUpdate(sqlNotification, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChildCommentItem item = items.get(i);

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, item.writerId());
                ps.setString(3, item.content());
                ps.setString(4, item.postId());
                ps.setTimestamp(5, Timestamp.valueOf(item.createdAt()));
                ps.setTimestamp(6, Timestamp.valueOf(item.createdAt()));
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });
    }

    private void loadCommentIds() {
        String sql = "SELECT id, post_id FROM tb_comment";
        jdbcTemplate.query(sql, rs -> {
            String id = rs.getString("id");
            String postId = rs.getString("post_id");
            parentComments.add(new ParentCommentInfo(id, postId));
        });

        if (parentComments.isEmpty()) {
            throw new RuntimeException("❌ 부모 댓글이 없습니다! CommentSeeder를 먼저 실행해주세요.");
        }
    }

    private record ParentCommentInfo(
            String id,
            String postId
    ){}

    public record ChildCommentItem(
            String id,
            String content,
            String postId,
            String parentId,
            String writerId,
            LocalDateTime createdAt
    ){}
}
