package net.causw.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Setter
@Getter
public class HomePageResponseDto {
    private BoardResponseDto board;
    private Page<PostAllResponseDto> posts;

    private HomePageResponseDto(
            BoardResponseDto board,
            Page<PostAllResponseDto> posts
    ) {
        this.board = board;
        this.posts = posts;
    }

    public static HomePageResponseDto from(
            BoardResponseDto board,
            Page<PostAllResponseDto> posts
    ) {
        return new HomePageResponseDto(
                board,
                posts
        );
    }
}
