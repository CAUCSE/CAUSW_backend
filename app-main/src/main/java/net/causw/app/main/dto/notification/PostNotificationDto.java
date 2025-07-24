package net.causw.app.main.dto.notification;

import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostNotificationDto {
	private String title;
	private String body;

	public static PostNotificationDto of(Post post, Comment comment) {
		return PostNotificationDto.builder()
			.title(String.format("%s",
				post.getTitle()
			))
			.body(String.format("새 댓글 : %s",
				comment.getContent()))
			.build();
	}
}
