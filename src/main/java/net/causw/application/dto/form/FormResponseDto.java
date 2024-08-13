package net.causw.application.dto.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.application.dto.user.UserResponseDto;

import java.util.List;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class FormResponseDto {
    @Schema(description = "신청폼 id 값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "신청폼 이름", example = "form_example")
    private String title;

    @Schema(description = "신청폼 작성자")
    private UserResponseDto writer;

    @Schema(description = "신청 가능 학년")
    private Set<Integer> allowedGrades;

    @Schema(description = "신청서 질문")
    private List<QuestionResponseDto> questions;



}
