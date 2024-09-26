package net.causw.application.dto.form.response.reply.excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExcelReplyQuestionResponseDto {

    @Schema(description = "질문 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String questionId;

    @Schema(description = "질문 답변", example = "이건 이 질문에 대한 답변입니다.")
    private String questionAnswer;

    @Schema(description = "선택한 옵션 리스트", example = "[\"1. 1번 선지내용\", \"2. 2번 선지내용\", ...]")
    private List<String> selectedOptionTextList;

}
