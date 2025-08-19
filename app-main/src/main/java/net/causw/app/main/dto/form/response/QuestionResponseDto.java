package net.causw.app.main.dto.form.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.app.main.domain.model.enums.form.QuestionType;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDto {

    @Schema(description = "질문 id", example = "uuid 형식의 String 값입니다.")
    private String questionId;

    @Schema(description = "질문 타입", example = "SUBJECTIVE / OBJECTIVE")
    private QuestionType questionType;

    @Schema(description = "질문 번호", example = "1")
    private Integer questionNumber;

    @Schema(description = "질문 내용", example = "질문내용입니다.")
    private String questionText;

    @Schema(description = "복수 선택 여부(객관식일 경우만 존재)", example = "false")
    private Boolean isMultiple;

    @Schema(description = "객관식 선지 Dto List(객관식일 경우에만 존재)")
    private List<OptionResponseDto> optionResponseDtoList;

}
