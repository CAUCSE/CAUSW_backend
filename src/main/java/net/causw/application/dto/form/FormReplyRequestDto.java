package net.causw.application.dto.form;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormReplyRequestDto {
    @Schema(description = "질문 답변")
    private List<QuestionReplyRequestDto> replyDtos;

    @Schema(description = "게시판 id")
    private String boardId;
}
