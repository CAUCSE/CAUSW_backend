package net.causw.app.main.domain.community.comment.service.dto;

/**
 * 댓글 생성 요청 데이터.
 *
 * <p>API 레이어에서 변환되어 {@link net.causw.app.main.domain.community.comment.service.CommentService#createComment}로 전달됩니다.</p>
 *
 * @param content     댓글 내용
 * @param postId      댓글을 작성할 게시글 ID
 * @param isAnonymous 익명 작성 여부
 * @param creatorId   댓글 작성자 ID
 */
public record CommentCreateCommand(
	String content,
	String postId,
	Boolean isAnonymous,
	String creatorId) {
}
