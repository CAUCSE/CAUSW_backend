package net.causw.application.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionCreateRequestDto {
    @Schema(description = "객관식 번호", example = "1", defaultValue = "1")
    private Integer optionNumber;

    @Schema(description = "객관식 내용", example = "1번 선지입니다.")
    private String optionText;

    @Schema(description = "객관식 선택 여부", defaultValue = "false")
    private Boolean isSelected;
}
