package net.causw.application.dto.form.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
public class OptionCreateRequestDto {

    @NotNull(message = "객관식 내용을 입력해 주세요.")
    @Schema(description = "객관식 내용(없으면 공백 문자열이라도 입력해주세요)", example = "1번 선지입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String optionText;

}
