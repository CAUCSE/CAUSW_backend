package net.causw.app.main.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateRequestDto {
    @NotBlank(message = "댓글 내용을 입력해 주세요.")
    private String content;

    @NotBlank(message = "게시물 id를 입력해 주세요.")
    private String postId;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

}
