package net.causw.application.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChildCommentCreateRequestDto {

    @NotBlank(message = "대댓글 내용을 입력해 주세요.")
    private String content;

    @NotBlank(message = "참고 대댓글을 선택해 주세요.")
    private String refChildComment;

    @NotBlank(message = "부모 댓글을 선택해 주세요.")
    private String parentCommentId;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    public Optional<String> getRefChildComment() {
        return Optional.ofNullable(this.refChildComment);
    }
}
