package net.causw.application.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionCreateRequestDto {
    @NotNull(message = "객관식 번호를 입력해 주세요.")
    @Schema(description = "객관식 번호", example = "1", defaultValue = "1")
    private Integer optionNumber;

    @NotBlank(message = "객관식 내용을 입력해 주세요.")
    @Schema(description = "객관식 내용", example = "1번 선지입니다.")
    private String optionText;

    @NotNull(message = "객관식 선택 여부를 선택해 주세요.")
    @Schema(description = "객관식 선택 여부", defaultValue = "false")
    private Boolean isSelected;
}
