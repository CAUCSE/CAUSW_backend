package net.causw.application.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OptionResponseDto {

    @Schema(description = "객관식 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "객관식 번호", example = "1")
    private Integer optionNumber;

    @Schema(description = "객관식 문항", example = "1번입니다.")
    private String optionText;

    @Schema(description = "객관식 선택 여부", example = "false")
    private Boolean isSelected;
}
