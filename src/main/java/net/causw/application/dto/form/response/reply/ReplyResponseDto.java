package net.causw.application.dto.form.response.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReplyResponseDto {

    @Schema(description = "답변 제출 사용자 정보 Dto")
    private ReplyUserResponseDto replyUserResponseDto;

    @Schema(description = "해당 사용자의 질문별 답변 리스트")
    private List<ReplyQuestionResponseDto> replyQuestionResponseDtoList;

    @Schema(description = "답변 제출 시간", example = "2021-08-31T15:00:00")
    private LocalDateTime createdAt;

}
