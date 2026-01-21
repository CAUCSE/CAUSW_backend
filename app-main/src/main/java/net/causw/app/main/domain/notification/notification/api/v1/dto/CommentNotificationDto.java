package net.causw.app.main.domain.notification.notification.api.v1.dto;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentNotificationDto {
	private String title;
	private String body;

	public static CommentNotificationDto of(Comment comment, ChildComment childComment) {
		return CommentNotificationDto.builder()
			.title(String.format("%s",
				comment.getContent()))
			.body(String.format("새 대댓글 : %s",
				childComment.getContent()))
			.build();
	}
}
