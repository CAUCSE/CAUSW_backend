package net.causw.application.dto.form;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionReplyResponseDto {
    private String questionId;
    private String questionAnswer;
    private List<Integer> selectedOptions;
}