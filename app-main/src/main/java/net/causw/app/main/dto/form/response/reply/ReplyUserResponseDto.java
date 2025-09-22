package net.causw.app.main.dto.form.response.reply;

import java.time.LocalDateTime;

import net.causw.app.main.domain.model.enums.user.Department;
import net.causw.app.main.domain.model.enums.user.GraduationType;
import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyUserResponseDto {

	@Schema(description = "고유 id값", example = "uuid 형식의 String 값입니다.")
	private String userId;

	@Schema(description = "이메일(아이디)")
	private String email;

	@Schema(description = "이름", example = "정상제")
	private String name;

	@Schema(description = "닉네임", example = "푸앙")
	private String nickName;

	@Schema(description = "입학년도", example = "2019")
	private Integer admissionYear;

	@Schema(description = "학번", example = "20191234")
	private String studentId;

	// TODO: user 테이블의 major 필드 삭제 후 함께 제거
	@Schema(description = "학부/학과", example = "컴퓨터공학과")
	private String major;

	@Schema(description = "학부/학과", example = "DEPT_OF_CS(전자계산학과), DEPT_OF_CSE(컴퓨터공학과), SCHOOL_OF_CSE(컴퓨터공학부), SCHOOL_OF_SW(소프트웨어학부), DEPT_OF_AI(AI학과)")
	private Department department;

	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNumber;

	@Schema(description = "학적상태", example = "ENROLLED")
	private AcademicStatus academicStatus;

	@Schema(description = "현재 학기", example = "1")
	private Integer currentCompletedSemester;

	@Schema(description = "졸업년도", example = "2023")
	private Integer graduationYear;

	@Schema(description = "졸업 유형", example = "FEBRUARY / AUGUST")
	private GraduationType graduationType;

	@Schema(description = "가입일", example = "2021-01-01T00:00:00")
	private LocalDateTime createdAt;

	@Schema(description = "본 학기 학생회비 적용 여부", example = "true")
	private Boolean isAppliedThisSemester;

	@Schema(description = "납부 시점 학기", example = "1")
	private Integer paidAt;

	@Schema(description = "납부한 학기 수", example = "8")
	private Integer numOfPaidSemester;

	@Schema(description = "잔여 학생회비 적용 학기", example = "3")
	private Integer restOfSemester;

	@Schema(description = "환불 여부", example = "false")
	private Boolean isRefunded;

}
