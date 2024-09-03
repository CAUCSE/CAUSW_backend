package net.causw.application.dto.form;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionSummaryResponseDto {
    private String questionId;
    private String questionText;
    private List<String> questionAnswers;
    private List<OptionSummaryResponseDto> optionSummaries;
}