package net.causw.application.dto.form.response.reply.excel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import net.causw.application.dto.form.response.QuestionResponseDto;
import net.causw.application.dto.form.response.reply.ReplyResponseDto;

import java.util.List;

@Getter
@Builder
public class ExcelReplyListResponseDto {

    @Schema(description = "질문 Dto List")
    private List<QuestionResponseDto> questionResponseDtoList;

    @Schema(description = "답변 Dto List")
    private List<ExcelReplyResponseDto> excelReplyResponseDtoList;

}
