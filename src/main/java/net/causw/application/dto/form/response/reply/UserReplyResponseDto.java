package net.causw.application.dto.form.response.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReplyResponseDto {
    @Schema(description = "해당 사용자의 질문별 답변 리스트")
    private List<ReplyQuestionResponseDto> replyQuestionResponseDtoList;
}
