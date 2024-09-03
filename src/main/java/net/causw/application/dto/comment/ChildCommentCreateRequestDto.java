package net.causw.application.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
    private String content;
    private String parentCommentId;
    private String refChildComment;

    @Schema(description = "익명글 여부", example = "False")
    private Boolean isAnonymous;

    public Optional<String> getRefChildComment() {
        return Optional.ofNullable(this.refChildComment);
    }
}
