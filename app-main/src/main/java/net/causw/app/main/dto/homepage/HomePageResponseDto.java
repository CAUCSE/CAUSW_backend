package net.causw.app.main.dto.homepage;

import org.springframework.data.domain.Page;

import net.causw.app.main.dto.board.BoardResponseDto;
import net.causw.app.main.dto.post.PostsResponseDto;

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
		Page<PostsResponseDto> posts
	) {
		return HomePageResponseDto.builder()
			.board(board)
			.posts(posts)
			.build();
	}
}
