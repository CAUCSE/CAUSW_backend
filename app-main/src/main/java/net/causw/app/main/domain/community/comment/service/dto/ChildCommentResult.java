package net.causw.app.main.domain.community.comment.service.dto;

import java.time.LocalDateTime;

/**
 * 대댓글 단건 응답 데이터.
 *
 * <p>Service 레이어에서 조립되어 {@link CommentResult#childCommentList()} 또는
 * API 레이어의 Response DTO로 변환됩니다.</p>
 *
 * @param id                 대댓글 ID
 * @param content            대댓글 내용 (작성자가 차단된 경우 {@code null})
 * @param createdAt          대댓글 작성 시각
 * @param updatedAt          대댓글 최종 수정 시각
 * @param authorInfo         작성자 정보 및 접근 권한 정보
 * @param isChildCommentLike 현재 조회 유저가 이 대댓글에 좋아요를 눌렀는지 여부
 * @param numLike            좋아요 수
 */
public record ChildCommentResult(
	String id,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	CommentAuthorInfo authorInfo,
	Boolean isChildCommentLike,
	Long numLike) {
}
