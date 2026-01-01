package net.causw.app.main.shared.seed;

import net.causw.app.main.shared.util.HotPostSampler;
import net.causw.app.main.shared.util.UserSegmenter;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Profile("seed")
public class CommentSeeder extends BasePostSeeder<CommentSeeder.CommentItem>{

    private final HotPostSampler hotPostSampler;

    public CommentSeeder(JdbcTemplate jdbcTemplate, UserSegmenter userSegmenter, HotPostSampler hotPostSampler) {
        super(jdbcTemplate, userSegmenter, 150_000, 1_000);
        this.hotPostSampler = hotPostSampler;
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.COMMENT;
    }

    @Override
    protected void beforeSeeding() {
        hotPostSampler.init();
    }

    @Override
    protected CommentItem createItem(int index) {
        // 1. 부모 메서드를 통해 확률에 맞는 작성자 선정
        String writerId = pickUser();
        // 2. 메트칼프의 법칙에 따라 대상 게시글 선정
        PostMetaData targetPost = hotPostSampler.pickHotPost();
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(totalCount - index);

        return new CommentItem(
                UUID.randomUUID().toString(),
                "Comment " + index,
                targetPost.id(),
                writerId,
                createdAt
        );
    }

    @Override
    protected void batchInsert(List<CommentItem> items) {
        // 1. 댓글 삽입
        String sqlComment = "INSERT INTO tb_comment (id, content, post_id, user_id, is_anonymous, is_deleted, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, false, false, ?, ?)";

        jdbcTemplate.batchUpdate(sqlComment, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CommentItem item = items.get(i);

                ps.setString(1, item.id());
                ps.setString(2, item.content());
                ps.setString(3, item.postId());
                ps.setString(4, item.writerId());
                ps.setTimestamp(5, Timestamp.valueOf(item.createdAt()));
                ps.setTimestamp(6, Timestamp.valueOf(item.createdAt()));
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });

        // 2. 구독자 연결
        String sqlSubscribe = "INSERT INTO tb_user_comment_subscribe " +
                "(id, is_subscribed, comment_id, user_id, created_at, updated_at) " +
                "VALUES (?, true, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sqlSubscribe, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CommentItem item = items.get(i);

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, item.id);
                ps.setString(3, item.writerId());
                ps.setTimestamp(4, Timestamp.valueOf(item.createdAt()));
                ps.setTimestamp(5, Timestamp.valueOf(item.createdAt()));
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });

        // 3. 알림 삽입
        String sqlNotification = "INSERT INTO tb_notification " +
                "(id, user_id, content, notice_type, target_id, is_global, created_at, updated_at) " +
                "VALUES (?, ?, ?, 'POST', ?, false, ?, ?)";

        jdbcTemplate.batchUpdate(sqlNotification, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CommentItem item = items.get(i);

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

    public record CommentItem(
            String id,
            String content,
            String postId,
            String writerId,
            LocalDateTime createdAt
    ){}
}
