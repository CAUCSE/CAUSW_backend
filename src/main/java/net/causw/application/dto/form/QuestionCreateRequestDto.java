package net.causw.application.dto.form;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCreateRequestDto {
    private Integer questionNumber;
    private String questionText;
    private Boolean isMultiple;
    private List<OptionCreateRequestDto> options;
}
