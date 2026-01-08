package net.causw.app.main.shared.seed;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import net.causw.app.main.shared.util.HotPostSampler;
import net.causw.app.main.shared.util.UserSegmenter;

@Component
@Profile("seed")
public class InteractionSeeder extends BasePostSeeder<InteractionSeeder.LikeItem> {

	private final HotPostSampler hotPostSampler;

	public InteractionSeeder(JdbcTemplate jdbcTemplate, UserSegmenter userSegmenter, HotPostSampler hotPostSampler) {
		super(jdbcTemplate, userSegmenter, 500_000, 1_000);
		this.hotPostSampler = hotPostSampler;
	}

	@Override
	protected ActionType getActionType() {
		return ActionType.LIKE;
	}

	@Override
	protected void beforeSeeding() {
		hotPostSampler.init();
	}

	@Override
	protected LikeItem createItem(int index) {
		// 1. 부모 메서드를 통해 확률에 맞는 작성자 선정
		String userId = pickUser();
		// 2. 메트칼프의 법칙에 따라 대상 게시글 선정
		PostMetaData targetPost = hotPostSampler.pickHotPost();
		LocalDateTime createdAt = LocalDateTime.now().minusMinutes(totalCount - index);

		return new LikeItem(
			UUID.randomUUID().toString(),
			targetPost.id(),
			userId,
			createdAt);
	}

	@Override
	protected void batchInsert(List<LikeItem> items) {
		// NOTE: 중복이더라도 업데이트가 지속되도록 DB 단에서 방어, 따라서 예상보다 시딩이 덜 될 수는 있음.
		String sqlLike = "INSERT IGNORE INTO tb_like_post (id, post_id, user_id, created_at, updated_at) " +
			"VALUES (?, ?, ?, ?, ?)";

		jdbcTemplate.batchUpdate(sqlLike, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				LikeItem item = items.get(i);
				Timestamp timestamp = Timestamp.valueOf(item.createdAt());

				ps.setString(1, item.id());
				ps.setString(2, item.postId());
				ps.setString(3, item.userId());
				ps.setTimestamp(4, timestamp);
				ps.setTimestamp(5, timestamp);
			}

			@Override
			public int getBatchSize() {
				return items.size();
			}
		});
	}

	public record LikeItem(
		String id,
		String postId,
		String userId,
		LocalDateTime createdAt) {
	}
}
