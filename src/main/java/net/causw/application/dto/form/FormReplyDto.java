package net.causw.application.dto.form;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormReplyDto {
    @Schema(description = "신청서 id", example = "uuid 형식의 String 값입니다.")
    private String formId;

    @Schema(description = "질문 답변")
    private List<QuestionReplyDto> replyDtos;
}
