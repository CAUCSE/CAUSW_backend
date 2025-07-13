package net.causw.app.main.dto.form.response.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyQuestionResponseDto {

    @Schema(description = "질문 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String questionId;

    @Schema(description = "질문 답변", example = "이건 이 질문에 대한 답변입니다.")
    private String questionAnswer;

    @Schema(description = "선택한 옵션 리스트", example = "[1, 2, 3]")
    private List<Integer> selectedOptionList;

}