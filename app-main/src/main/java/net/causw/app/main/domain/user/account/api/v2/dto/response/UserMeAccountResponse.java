package net.causw.app.main.domain.user.account.api.v2.dto.response;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.auth.enums.OnboardingStatus;
import net.causw.app.main.shared.dto.ProfileImageDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정정보 관리 조회 응답")
public record UserMeAccountResponse(

	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000") String id,

	@Schema(description = "이메일", example = "user@example.com") String email,

	@Schema(description = "이름", example = "홍길동") String name,

	@Schema(description = "닉네임", example = "푸앙") String nickname,

	@Schema(description = "프로필 이미지 정보") ProfileImageDto profileImage,

	@Schema(description = "입학년도", example = "2020") Integer admissionYear,

	@Schema(description = "졸업년도", example = "2026") Integer graduationYear,

	@Schema(description = "직업", example = "개발자", nullable = true) String job,

	@Schema(description = "온보딩 플로우 분기 상태", example = "ACTIVE") OnboardingStatus onboardingStatus,

	@Schema(description = "현재 학적 상태", example = "ENROLLED") AcademicStatus academicStatus,

	@Schema(description = "전화번호", example = "010-1234-5678", nullable = true) String phoneNumber,

	@Schema(description = "학번", example = "20201234", nullable = true) String studentId,

	@Schema(description = "전공 (legacy)", example = "컴퓨터공학", nullable = true) String major,

	@Schema(description = "학과", example = "SCHOOL_OF_SW", nullable = true) Department department) {
}
