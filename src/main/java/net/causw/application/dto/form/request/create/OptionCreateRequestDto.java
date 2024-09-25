package net.causw.application.dto.form.request.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
public class OptionCreateRequestDto {

    @NotBlank(message = "객관식 내용을 입력해 주세요.")
    @Schema(description = "객관식 내용", example = "1번 선지입니다.")
    private String optionText;

}
