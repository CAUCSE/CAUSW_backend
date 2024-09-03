package net.causw.application.dto.form;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCreateRequestDto {
    @Schema(description = "질문 번호", example = "1")
    private Integer questionNumber;

    @Schema(description = "질문 내용", example = "1번 문제입니다.")
    private String questionText;

    @Schema(description = "복수 정답 여부(객관식일 때만)", defaultValue = "false")
    private Boolean isMultiple;

    @Schema(description = "객관식")
    private List<OptionCreateRequestDto> options;
}
