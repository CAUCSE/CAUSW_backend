package net.causw.application.dto.form.response.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import net.causw.application.dto.form.response.QuestionResponseDto;

import java.util.List;

@Builder
public class ReplyOneResponseDto {

    @Schema(description = "질문 Dto List")
    private List<QuestionResponseDto> questionResponseDtoList;

    @Schema(description = "답변 Dto")
    private ReplyResponseDto replyResponseDto;

}
