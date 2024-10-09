package net.causw.application.dto.form.response.reply.excel;

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
public class ExcelReplyListResponseDto {

    @Schema(description = "질문 Dto List")
    private List<QuestionResponseDto> questionResponseDtoList;

    @Schema(description = "답변 Dto List")
    private List<ExcelReplyResponseDto> excelReplyResponseDtoList;

}
