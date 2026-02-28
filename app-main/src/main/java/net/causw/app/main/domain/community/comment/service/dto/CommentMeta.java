package net.causw.app.main.domain.community.comment.service.dto;

import java.util.Map;
import java.util.Set;

/**
 * 댓글 단건 렌더링에 필요한 모든 집계 데이터.
 *
 * <p>목록 조회 시에는 {@code CommentMetaReader.fetch()}가 {@code Map<commentId, CommentMeta>} 형태로 배치 조회한 결과를 담고,
 * 단건 작업(수정·삭제) 시에는 {@code CommentMetaReader.fetchForComment()}가 단건 조회한 결과를 담습니다.</p>
 *
 * @param numLike         이 댓글의 좋아요 수
 * @param isLiked         현재 조회 유저가 이 댓글에 좋아요를 눌렀는지 여부
 * @param isSubscribed    현재 조회 유저가 이 댓글을 구독했는지 여부
 * @param isBlocked       이 댓글 작성자가 현재 조회 유저에 의해 차단됐는지 여부
 * @param childLikeCounts 이 댓글에 속한 대댓글들의 좋아요 수 {@code Map<대댓글ID, 좋아요 수>}
 * @param likedChildIds   현재 조회 유저가 좋아요를 누른 대댓글 ID 집합 (이 댓글의 자식만)
 * @param blockedChildIds 차단된 사용자가 작성한 대댓글 ID 집합 (이 댓글의 자식만)
 */
public record CommentMeta(
	long numLike,
	boolean isLiked,
	boolean isSubscribed,
	boolean isBlocked,
	Map<String, Long> childLikeCounts,
	Set<String> likedChildIds,
	Set<String> blockedChildIds) {

	/**
	 * 신규 댓글 생성 직후 응답에 사용하는 빈 메타 데이터를 반환합니다.
	 * 좋아요 0, 구독·차단·대댓글 없음 상태입니다.
	 */
	public static CommentMeta forNew() {
		return new CommentMeta(0L, false, false, false, Map.of(), Set.of(), Set.of());
	}
}
