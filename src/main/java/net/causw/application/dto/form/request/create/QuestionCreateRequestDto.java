package net.causw.application.dto.form.request.create;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.causw.domain.model.enums.form.QuestionType;

import java.util.List;

@Getter
public class QuestionCreateRequestDto {

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
    private List<OptionCreateRequestDto> optionCreateRequestDtoList;
}
