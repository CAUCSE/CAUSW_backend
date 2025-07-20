package net.causw.app.main.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PostCreateResponseDto {

    @Schema(description = "게시글 id", example = "uuid 형식의 String 값입니다.")
    private String id;

}




