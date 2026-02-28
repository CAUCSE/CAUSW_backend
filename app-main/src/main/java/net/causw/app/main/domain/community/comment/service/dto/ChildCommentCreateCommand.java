package net.causw.app.main.domain.community.comment.service.dto;

/**
 * 대댓글 생성 요청 데이터.
 *
 * <p>API 레이어에서 변환되어 {@link net.causw.app.main.domain.community.comment.service.ChildCommentService#createChildComment}로 전달됩니다.</p>
 *
 * @param content         대댓글 내용
 * @param parentCommentId 부모 댓글 ID
 * @param isAnonymous     익명 작성 여부
 * @param creatorId       대댓글 작성자 ID
 */
public record ChildCommentCreateCommand(
	String content,
	String parentCommentId,
	Boolean isAnonymous,
	String creatorId) {

}
