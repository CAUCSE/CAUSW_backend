package net.causw.application.dto.form.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.enums.form.QuestionType;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionSummaryResponseDto {

    @Schema(description = "질문 id", example = "uuid 형식의 String 값입니다.")
    private String questionId;

    @Schema(description = "질문 타입", example = "SUBJECTIVE / OBJECTIVE")
    private QuestionType questionType;

    @Schema(description = "질문 번호", example = "1")
    private String questionText;

    @Schema(description = "질문 답변 내용 List(주관식 질문일 경우)", example = "[답변1, 답변2, ...]")
    private List<String> questionAnswerList;

    @Schema(description = "질문 답변 정보 List(객관식 질문일 경우)", example = "[OptionSummaryResponseDto, OptionSummaryResponseDto, ...]")
    private List<OptionSummaryResponseDto> optionSummarieList;

}