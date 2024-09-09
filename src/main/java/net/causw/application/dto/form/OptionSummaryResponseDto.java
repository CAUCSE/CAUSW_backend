package net.causw.application.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionSummaryResponseDto {
    private Integer optionNumber;
    private String optionText;
    private Long selectedCount;
}