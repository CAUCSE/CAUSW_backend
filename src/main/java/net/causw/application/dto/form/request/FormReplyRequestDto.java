package net.causw.application.dto.form.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
public class FormReplyRequestDto {

    @NotNull(message = "답변 정보는 필수입니다.")
    @Schema(description = "질문 답변", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<QuestionReplyRequestDto> questionReplyRequestDtoList;

}
