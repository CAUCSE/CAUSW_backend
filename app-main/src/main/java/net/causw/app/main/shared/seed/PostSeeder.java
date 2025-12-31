package net.causw.app.main.shared.seed;

import net.causw.app.main.shared.util.DistributionUtils;
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
import java.util.Map;
import java.util.UUID;

@Component
@Profile("seed")
public class PostSeeder extends BasePostSeeder<PostSeeder.PostItem> {

    private String freeBoardId;
    private String humorBoardId;
    private List<String> otherBoardIds;

    public PostSeeder(JdbcTemplate jdbcTemplate, UserSegmenter userSegmenter) {
        super(jdbcTemplate, userSegmenter, 100_000, 1_000);
    }

    @Override
    protected ActionType getActionType() {
        return ActionType.POST;
    }

    @Override
    protected void beforeSeeding() {
        loadBoardIds();
    }

    @Override
    protected PostItem createItem(int index) {
        // 1. 부모 메서드를 통해 확률에 맞는 작성자 선정
        String writerId = pickUser();
        // 2. 파레토 법칙으로 게시판 선정
        String boardId = DistributionUtils.selectBoardId(freeBoardId, humorBoardId, otherBoardIds);
        String postId = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(totalCount - index);

        // 3. 이미지 시딩: 50% 확률로 0장, 나머지 50% 확률로 랜덤하게 1~3장
        List<PostImageItem> images = new ArrayList<>();
        if (Math.random() < 0.5) { // 50% 확률로 진입
            int imageCount = (int) (Math.random() * 3) + 1; // 1~3장

            for (int i = 0; i < imageCount; i++) {
                String fileUuid = UUID.randomUUID().toString();
                String fileKey = "seed/post/" + fileUuid + ".png";
                String fileUrl = "https://cdn.seed.test/post/" + fileUuid + ".png";

                images.add(new PostImageItem(
                        fileUuid,
                        postId,
                        fileKey,
                        fileUrl, // 확장자 png로 고정
                        createdAt
                ));
            }
        }

        return new PostItem(
                postId,
                writerId,
                boardId,
                "Seeding Title " + index,
                "This is seeding content for post " + index,
                createdAt,
                images
        );
    }

    @Override
    protected void batchInsert(List<PostItem> items) {
        // 1. 게시글 삽입
        String sqlPost = "INSERT INTO tb_post (id, user_id, board_id, title, content, created_at, updated_at, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, false)";

        jdbcTemplate.batchUpdate(sqlPost, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PostItem item = items.get(i);
                ps.setString(1, item.id);
                ps.setString(2, item.writerId());
                ps.setString(3, item.boardId());
                ps.setString(4, item.title());
                ps.setString(5, item.content());
                ps.setTimestamp(6, Timestamp.valueOf(item.createdAt()));
                ps.setTimestamp(7, Timestamp.valueOf(item.createdAt())); // updated_at도 동일하게
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });

        // 2. 이미지 삽입 및 연결
        List<PostImageItem> images = items.stream()
                .flatMap(item -> item.images().stream())
                .toList();

        if (!images.isEmpty()) {
            String sqlUuid = "INSERT INTO tb_uuid_file " +
                    "(id, uuid, file_key, raw_file_name, file_path, file_url, extension, created_at, updated_at) " +
                    "VALUES (?, ?, ?, 'post.png', 'POST', ?, 'png', ?, ?)";

            jdbcTemplate.batchUpdate(sqlUuid, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PostImageItem item = images.get(i);
                    ps.setString(1, item.id);
                    ps.setString(2, item.id);
                    ps.setString(3, item.fileKey());
                    ps.setString(4, item.fileUrl());
                    ps.setTimestamp(5, Timestamp.valueOf(item.createdAt()));
                    ps.setTimestamp(6, Timestamp.valueOf(item.createdAt())); // updated_at도 동일하게
                }

                @Override
                public int getBatchSize() {
                    return images.size();
                }
            });
            String sqlMapping = "INSERT INTO tb_post_attach_image_uuid_file " +
                    "(id, post_id, uuid_file_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.batchUpdate(sqlMapping, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PostImageItem item = images.get(i);
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, item.postId());
                    ps.setString(3, item.id());
                    ps.setTimestamp(4, Timestamp.valueOf(item.createdAt()));
                    ps.setTimestamp(5, Timestamp.valueOf(item.createdAt()));
                }

                @Override
                public int getBatchSize() {
                    return images.size();
                }
            });
        }

        // 3. 구독자 연결
        String sqlSub = "INSERT INTO tb_user_post_subscribe " +
                "(id, post_id, user_id, is_subscribed, created_at, updated_at) " +
                "VALUES (?, ?, ?, true, ?, ?)";

        jdbcTemplate.batchUpdate(sqlSub, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PostItem item = items.get(i);

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

        // 4. 알림 삽입
        String sqlNotification = "INSERT INTO tb_notification " +
                "(id, user_id, content, notice_type, target_id, is_global, created_at, updated_at) " +
                "VALUES (?, ?, ?, 'BOARD', ?, false, ?, ?)";

        jdbcTemplate.batchUpdate(sqlNotification, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PostItem item = items.get(i);
                String notificationId = UUID.randomUUID().toString();

                ps.setString(1, notificationId);
                ps.setString(2, item.writerId());
                ps.setString(3, item.title());
                ps.setString(4, item.id);
                ps.setTimestamp(5, Timestamp.valueOf(item.createdAt()));
                ps.setTimestamp(6, Timestamp.valueOf(item.createdAt()));
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });
    }

    private void loadBoardIds() {
        String query = "SELECT id, name FROM tb_board";

        List<Map<String, Object>> boards = jdbcTemplate.queryForList(query);

        List<String> others = new ArrayList<>();

        for (Map<String, Object> row : boards) {
            String id = (String) row.get("id"); // UUID String
            String name = (String) row.get("name");

            if ("자유게시판".equals(name)) {
                this.freeBoardId = id;
            } else if ("유머게시판".equals(name)) {
                this.humorBoardId = id;
            } else {
                others.add(id);
            }
        }
        this.otherBoardIds = others;

        // 예외 처리
        if (this.freeBoardId == null) throw new RuntimeException("자유게시판이 없습니다. BoardSeeder를 먼저 실행하세요.");
        if (this.humorBoardId == null) throw new RuntimeException("유머게시판이 없습니다. BoardSeeder를 먼저 실행하세요.");
    }

    public record PostItem(
            String id,
            String writerId,
            String boardId,
            String title,
            String content,
            LocalDateTime createdAt,
            List<PostImageItem> images
    ) {}

    public record PostImageItem(
            String id,
            String postId,
            String fileKey,
            String fileUrl,
            LocalDateTime createdAt
    ){}
}