package net.causw.application.dto.form.response.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import net.causw.application.dto.form.response.QuestionResponseDto;

import java.util.List;

@Getter
@Builder
public class ReplyListResponseDto {

    @Schema(description = "질문 Dto List")
    private List<QuestionResponseDto> questionResponseDtoList;

    @Schema(description = "답변 Dto List")
    private List<ReplyResponseDto> replyResponseDtoList;

}
