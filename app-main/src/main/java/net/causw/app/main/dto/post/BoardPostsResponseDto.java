package net.causw.app.main.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BoardPostsResponseDto {

    @Schema(description = "게시판 id", example = "uuid 형식의 String 값입니다.")
    private String boardId;

    @Schema(description = "게시판 이름", example = "게시판 이름입니다.")
    private String boardName;

    @Schema(description = "게시글 작성 가능 여부", example = "true")
    private Boolean writable;

    @Schema(description = "즐겨찾기 게시판 여부", example = "false")
    private Boolean isFavorite;

    @Schema(description = "게시판 구독 여부", example = "false")
    private Boolean isBoardSubscribed;


    @Schema(description = "게시글 정보입니다")
    private Page<PostsResponseDto> post;

}
