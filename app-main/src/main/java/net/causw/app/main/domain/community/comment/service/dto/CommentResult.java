package net.causw.app.main.domain.community.comment.service.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 댓글 단건 응답 데이터.
 *
 * <p>Service 레이어에서 조립되어 API 레이어의 Response DTO로 변환됩니다.</p>
 *
 * @param id                 댓글 ID
 * @param content            댓글 내용 (작성자가 차단된 경우 {@code null})
 * @param createdAt          댓글 작성 시각
 * @param updatedAt          댓글 최종 수정 시각
 * @param postId             이 댓글이 속한 게시글 ID
 * @param authorInfo         작성자 정보 및 접근 권한 정보
 * @param isCommentLike      현재 조회 유저가 이 댓글에 좋아요를 눌렀는지 여부
 * @param isCommentSubscribed 현재 조회 유저가 이 댓글을 구독했는지 여부
 * @param numLike            좋아요 수
 * @param numChildComment    대댓글 수
 * @param childCommentList   대댓글 목록
 */
public record CommentResult(
	String id,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String postId,
	CommentAuthorInfo authorInfo,
	Boolean isCommentLike,
	Boolean isCommentSubscribed,
	Long numLike,
	Long numChildComment,
	List<ChildCommentResult> childCommentList) {
}
