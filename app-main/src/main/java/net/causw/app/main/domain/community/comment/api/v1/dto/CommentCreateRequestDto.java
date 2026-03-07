package net.causw.app.main.domain.community.comment.api.v1.dto;

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

	@NotBlank(message = "댓글을 달 게시물이 선택되지 않았습니다..")
	private String postId;

	@Schema(description = "익명글 여부", example = "False")
	private Boolean isAnonymous;

}
