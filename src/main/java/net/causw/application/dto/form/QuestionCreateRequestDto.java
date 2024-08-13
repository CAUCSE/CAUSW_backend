package net.causw.application.dto.form;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionCreateRequestDto {
    private String questionText;
    private boolean isMultipleChoice;
    private List<String> options;
}
