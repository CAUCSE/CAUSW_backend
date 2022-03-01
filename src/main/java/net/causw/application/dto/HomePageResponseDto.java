package net.causw.application.dto;

import lombok.Getter;
import lombok.Setter;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class HomePageResponseDto {
    private BoardResponseDto board;
    private Page<PostsResponseDto> posts;

    private HomePageResponseDto(
            BoardResponseDto board,
            Page<PostsResponseDto> posts
    ) {
        this.board = board;
        this.posts = posts;
    }

    public static HomePageResponseDto from(
            BoardResponseDto board,
            Page<PostsResponseDto> posts
    ) {
        return new HomePageResponseDto(
                board,
                posts
        );
    }
}
