package net.causw.application.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateRequestDto {
    @NotBlank(message = "댓글 내용을 입력해 주세요.")
    private String content;
}
