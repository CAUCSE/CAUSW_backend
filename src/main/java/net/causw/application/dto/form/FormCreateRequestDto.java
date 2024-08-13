package net.causw.application.dto.form;


import lombok.*;
import net.causw.adapter.persistence.circle.Circle;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormCreateRequestDto {
    private String title;
    private Set<Integer> allowedGrades;
    private List<QuestionCreateRequestDto> questions;
//    private String circleId;
}
