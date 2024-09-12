package net.causw.application.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class QuestionResponseDto {

    @Schema(description = "질문 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "질문 번호", example = "1")
    private Integer questionNumber;

    @Schema(description = "질문 내용", example = "질문내용입니다.")
    private String questionText;

    @Schema(description = "복수 선택 여부", example = "false")
    private Boolean isMultiple;

    @Schema(description = "객관식")
    private List<OptionResponseDto> options;

}
