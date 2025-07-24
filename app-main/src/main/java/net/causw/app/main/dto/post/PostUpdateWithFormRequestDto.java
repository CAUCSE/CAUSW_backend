package net.causw.app.main.dto.post;

import net.causw.app.main.dto.form.request.create.FormCreateRequestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostUpdateWithFormRequestDto {

	@NotBlank(message = "게시글 제목을 입력해 주세요.")
	@Schema(description = "게시글 제목", example = "게시글의 제목입니다.")
	private String title;

	@NotBlank(message = "게시글 내용을 입력해 주세요.")
	@Schema(description = "게시글 내용", example = "게시글의 내용입니다.")
	private String content;

	@NotNull(message = "신청서 생성 요청 정보를 입력해 주세요.")
	@Schema(description = "신청서 생성 요청 정보")
	private FormCreateRequestDto formCreateRequestDto;

}
