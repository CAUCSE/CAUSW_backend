package net.causw.app.main.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChildCommentUpdateRequestDto {
	@NotBlank(message = "대댓글 내용을 입력해 주세요.")
	private String content;
}
