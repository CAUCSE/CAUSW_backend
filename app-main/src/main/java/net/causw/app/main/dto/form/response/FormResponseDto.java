package net.causw.app.main.dto.form.response;

import java.util.List;

import net.causw.app.main.domain.model.enums.form.RegisteredSemester;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormResponseDto {

	@Schema(description = "신청폼 id 값", example = "uuid 형식의 String 값입니다.")
	private String formId;

	@Schema(description = "신청폼 제목", example = "form_example")
	private String title;

	@Schema(description = "마감 여부", example = "false")
	private Boolean isClosed;

	@Schema(description = "재학생 답변 허용 여부", example = "true")
	private Boolean isAllowedEnrolled;

	@Schema(description = "재학생 답변 허용 시, 답변 가능한 등록 완료 학기(상관없음 시 1차부터 9차 이상까지)",
		example = "[FIRST_SEMESTER,SECOND_SEMESTER ... EIGHTH_SEMESTER,ABOVE_NIGHT_SEMESTER]")
	private List<RegisteredSemester> enrolledRegisteredSemesterList;

	@Schema(description = "재학생 답변 허용 시, 학생회비 납부 필요 여부", example = "ture")
	private Boolean isNeedCouncilFeePaid;

	@NotNull(message = "휴학생 답변 허용 여부를 선택해 주세요.")
	@Schema(description = "휴학생 답변 허용 여부", example = "ture")
	private Boolean isAllowedLeaveOfAbsence;

	@Schema(description = "휴학생 답변 허용 시, 답변 가능한 등록 완료 학기(상관없음 시 1차부터 9차 이상까지)",
		example = "[FIRST_SEMESTER,SECOND_SEMESTER ... EIGHTH_SEMESTER,ABOVE_NIGHT_SEMESTER]")
	private List<RegisteredSemester> leaveOfAbsenceRegisteredSemesterList;

	@Schema(description = "졸업생 답변 허용 여부", example = "ture")
	private Boolean isAllowedGraduation;

	@Schema(description = "신청서 질문 Dto List")
	private List<QuestionResponseDto> questionResponseDtoList;

	public static FormResponseDto empty() {
		return FormResponseDto.builder()
			.formId("")
			.title("")
			.isClosed(false)
			.isAllowedEnrolled(false)
			.enrolledRegisteredSemesterList(List.of())  // 빈 리스트
			.isNeedCouncilFeePaid(false)  // 기본값 false
			.isAllowedLeaveOfAbsence(false)  // 기본값 false
			.leaveOfAbsenceRegisteredSemesterList(List.of())  // 빈 리스트
			.isAllowedGraduation(false)  // 기본값 false
			.questionResponseDtoList(List.of())  // 빈 리스트
			.build();
	}
}
