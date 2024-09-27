package net.causw.application.dto.form.response.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.application.dto.form.response.QuestionResponseDto;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyOneResponseDto {

    @Schema(description = "질문 Dto List")
    private List<QuestionResponseDto> questionResponseDtoList;

    @Schema(description = "답변 Dto")
    private ReplyResponseDto replyResponseDto;

}
