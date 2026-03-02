package net.causw.app.main.domain.community.comment.service.dto;

/**
 * 댓글 수정 요청 데이터.
 *
 * <p>API 레이어에서 변환되어 {@link net.causw.app.main.domain.community.comment.service.CommentService#updateComment}로 전달됩니다.</p>
 *
 * @param commentId 수정할 댓글 ID
 * @param content   수정할 댓글 내용
 * @param updaterId 수정 요청 유저 ID
 */
public record CommentUpdateCommand(
	String commentId,
	String content,
	String updaterId) {
}
