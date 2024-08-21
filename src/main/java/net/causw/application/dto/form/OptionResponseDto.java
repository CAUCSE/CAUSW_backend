package net.causw.application.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class OptionResponseDto {
    @Schema(description = "객관식 번호", example = "1")
    private Integer optionNumber;

    @Schema(description = "객관식 문항", example = "1번입니다.")
    private String optionText;

    @Schema(description = "객관식 선택 여부")
    private Boolean isSelected;
}
