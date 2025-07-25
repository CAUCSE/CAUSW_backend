package net.causw.app.main.dto.form.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionResponseDto {

    @Schema(description = "객관식 id", example = "uuid 형식의 String 값입니다.")
    private String optionId;

    @Schema(description = "객관식 번호", example = "1")
    private Integer optionNumber;

    @Schema(description = "객관식 문항", example = "1번입니다.")
    private String optionText;

}
