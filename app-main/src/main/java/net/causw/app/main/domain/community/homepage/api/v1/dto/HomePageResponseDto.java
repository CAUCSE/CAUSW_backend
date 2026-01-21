package net.causw.app.main.domain.community.homepage.api.v1.dto;

import org.springframework.data.domain.Page;

import net.causw.app.main.domain.community.board.api.v1.dto.BoardResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostsResponseDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HomePageResponseDto {
	private BoardResponseDto board;
	private Page<PostsResponseDto> posts;

	public static HomePageResponseDto of(
		BoardResponseDto board,
		Page<PostsResponseDto> posts) {
		return HomePageResponseDto.builder()
			.board(board)
			.posts(posts)
			.build();
	}
}
