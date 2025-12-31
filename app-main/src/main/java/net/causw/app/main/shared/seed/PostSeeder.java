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
    protected void beforeSeeding() {
        loadBoardIds();
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

    @Override
    protected ActionType getActionType() {
        return ActionType.POST;
    }

    @Override
    protected PostItem createItem(int index) {
        // 1. 부모 메서드를 통해 확률에 맞는 작성자 선정
        String writerId = pickUser();

        // 2. 파레토 법칙으로 게시판 선정
        String boardId = DistributionUtils.selectBoardId(freeBoardId, humorBoardId, otherBoardIds);

        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(totalCount - index);

        return new PostItem(
                UUID.randomUUID().toString(),
                writerId,
                boardId,
                "Seeding Title " + index,
                "This is seeding content for post " + index,
                createdAt
        );
    }

    @Override
    protected void batchInsert(List<PostItem> items) {
        String sql = "INSERT INTO tb_post (id, user_id, board_id, title, content, created_at, updated_at, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, false)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
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
    }

    public record PostItem(
            String id,
            String writerId,
            String boardId,
            String title,
            String content,
            LocalDateTime createdAt
    ) {}
}