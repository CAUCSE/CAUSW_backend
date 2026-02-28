package net.causw.app.main.domain.community.comment.service.dto;

import net.causw.app.main.domain.user.account.entity.user.User;

/**
 * 대댓글 수정 요청 데이터.
 *
 * <p>API 레이어에서 변환되어 {@link net.causw.app.main.domain.community.comment.service.ChildCommentService#updateChildComment}로 전달됩니다.</p>
 *
 * @param childCommentId 수정할 대댓글 ID
 * @param content        수정할 대댓글 내용
 * @param updater        수정 요청 유저
 */
public record ChildCommentUpdateCommand(
	String childCommentId,
	String content,
	User updater) {
}
