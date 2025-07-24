package net.causw.app.main.dto.form.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FormReplyRequestDto {

	@NotNull(message = "답변 정보는 필수입니다.")
	@Schema(description = "질문 답변", requiredMode = Schema.RequiredMode.REQUIRED)
	private List<QuestionReplyRequestDto> questionReplyRequestDtoList;

}
