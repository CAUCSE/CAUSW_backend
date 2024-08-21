package net.causw.application.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.circle.Circle;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class QuestionResponseDto {

    @Schema(description = "질문 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "질문 번호")
    private Integer questionNumber;

    @Schema(description = "질문 내용")
    private String questionText;

    @Schema(description = "복수 선택 여부")
    private Boolean isMultiple;

    @Schema(description = "질문 내용")
    private List<OptionResponseDto> options;

}
