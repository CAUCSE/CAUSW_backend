package net.causw.application.dto.form.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public class OptionSummaryResponseDto {

    @Schema(description = "객관식 id", example = "uuid 형식의 String 값입니다.")
    private String optionId;

    @Schema(description = "객관식 번호", example = "1")
    private Integer optionNumber;

    @Schema(description = "객관식 문항 내용", example = "1번 선지 내용입니다.")
    private String optionText;

    @Schema(description = "선택된 횟수", example = "3")
    private Long selectedCount;

}