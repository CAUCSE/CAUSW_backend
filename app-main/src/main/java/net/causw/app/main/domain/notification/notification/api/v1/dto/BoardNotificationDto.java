package net.causw.app.main.domain.notification.notification.api.v1.dto;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardNotificationDto {
	private String title;
	private String body;

	public static BoardNotificationDto of(Board board, Post post) {
		return BoardNotificationDto.builder()
			.title(String.format("%s",
				board.getName()))
			.body(String.format("새 게시글 : %s",
				post.getTitle()))
			.build();
	}
}
