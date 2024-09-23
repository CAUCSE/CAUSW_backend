package net.causw.application.dto.form;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.causw.domain.model.enums.QuestionType;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCreateRequestDto {
    @NotNull(message = "질문 번호를 입력해 주세요.")
    @Schema(description = "질문 번호", example = "1")
    private Integer questionNumber;

    @NotBlank(message = "질문 내용을 입력해 주세요.")
    @Schema(description = "질문 종류", example = "SUBJECTIVE")
    private QuestionType questionType;

    @NotBlank(message = "질문 내용을 입력해 주세요.")
    @Schema(description = "질문 내용", example = "1번 문제입니다.")
    private String questionText;

    @NotNull(message = "복수 정답 여부를 선택해 주세요.")
    @Schema(description = "복수 정답 여부(객관식일 때만)", defaultValue = "false")
    private Boolean isMultiple;

    @Schema(description = "객관식")
    private List<OptionCreateRequestDto> options;
}
