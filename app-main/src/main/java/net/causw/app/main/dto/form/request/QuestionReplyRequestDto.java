package net.causw.app.main.dto.form.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
public class QuestionReplyRequestDto {

    @NotBlank(message = "질문 id를 입력해 주세요.")
    @Schema(description = "질문 id", example = "uuid 형식의 String 값입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String questionId;

    @NotBlank(message = "질문에 응답해 주세요.")
    @Schema(description = "질문 응답(주관식)", example = "1번 주관식 응답입니다.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String questionReply;

    @Schema(description = "객관식 선택 번호(객관식)", example = "[1, 2]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<Integer> selectedOptionList;

}
