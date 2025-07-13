package net.causw.app.main.dto.form.response.reply.excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.app.main.dto.form.response.reply.ReplyUserResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelReplyResponseDto {

    @Schema(description = "답변 제출 사용자 정보 Dto")
    private ReplyUserResponseDto replyUserResponseDto;

    @Schema(description = "해당 사용자의 질문별 답변 리스트")
    private List<ExcelReplyQuestionResponseDto> excelReplyQuestionResponseDtoList;

    @Schema(description = "답변 제출 시간", example = "2021-08-31T15:00:00")
    private LocalDateTime createdAt;

}
