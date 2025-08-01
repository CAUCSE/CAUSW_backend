package net.causw.app.main.dto.form.request.create;

import java.util.List;

import net.causw.app.main.domain.model.enums.form.QuestionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class QuestionCreateRequestDto {

	@NotBlank(message = "질문 내용을 입력해 주세요.")
	@Schema(description = "질문 종류", example = "SUBJECTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
	private QuestionType questionType;

	@NotNull(message = "질문 내용을 입력해 주세요.")
	@Schema(description = "질문 내용, 없으면 공백 문자열이라도 보내줘야 합니다.", example = "1번 문제입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
	private String questionText;

	@Schema(description = "복수 정답 여부(객관식일 때만)", defaultValue = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private Boolean isMultiple;

	@Schema(description = "객관식", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private List<OptionCreateRequestDto> optionCreateRequestDtoList;
}
