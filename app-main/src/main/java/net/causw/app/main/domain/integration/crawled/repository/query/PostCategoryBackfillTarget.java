package net.causw.app.main.domain.integration.crawled.repository.query;

/**
 * 성격 백필 대상 조회 결과입니다.
 *
 * <p>수정 시각이 갱신되지 않도록 Post 엔티티 대신 필요한 값만 투영합니다.</p>
 *
 * @param noticeId 다음 조회에 사용할 공지 식별자
 * @param postId 성격을 채울 게시글 식별자
 * @param title 공지 제목
 */
public record PostCategoryBackfillTarget(
	String noticeId,
	String postId,
	String title) {
}
