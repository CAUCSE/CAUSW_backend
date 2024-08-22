package net.causw.application.dto.form;


import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "신청서 제목", example = "신청서 제목입니다.")
    private String title;

    @Schema(description = "신청서 작성 가능 학년")
    private Set<Integer> allowedGrades;

    @Schema(description = "질문")
    private List<QuestionCreateRequestDto> questions;

    @Schema(description = "동아리 id", example = "uuid 형식의 String 값입니다(nullable).")
    private String circleId;
}
