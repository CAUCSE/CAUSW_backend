package net.causw.application.dto.form;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import net.causw.domain.model.enums.AcademicStatus;

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

    @NotEmpty(message = "신청서를 작성할 수 있는 학년을 입력해 주세요.")
    @Schema(description = "신청서 작성 가능 학년")
    private Set<Integer> allowedGrades;

    @Schema(description = "신청서 작성 가능 학적 상태")
    private AcademicStatus allowedAcademicStatus;

    @Schema(description = "학생회비 납부 여부")
    private Boolean isPaid;


    @NotEmpty(message = "질문을 입력해 주세요.")
    @Schema(description = "질문")
    private List<QuestionCreateRequestDto> questions;

    @Schema(description = "동아리 id", example = "uuid 형식의 String 값입니다(nullable).")
    private String circleId;
}
