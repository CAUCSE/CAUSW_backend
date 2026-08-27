package net.causw.app.main.domain.integration.crawled.repository.query;

/**
 * 성격 백필 대상입니다. 수정 시각이 갱신되지 않도록 Post 엔티티 대신 필요한 값만 투영합니다.
 */
public record PostCategoryBackfillTarget(
	String noticeId,
	String postId,
	String title) {
}
