package net.causw.app.main.dto.homepage;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.dto.board.BoardResponseDto;
import org.springframework.data.domain.Page;

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
