package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 유저 목록 조회 응답 아이템")
public record UserListItemResponse(
	@Schema(description = "유저 ID", example = "user-1") String id,

	@Schema(description = "이름", example = "홍길동") String name,

	@Schema(description = "학번", example = "20201234") String studentId,

	@Schema(description = "소속 학과", example = "SCHOOL_OF_SW") Department department,

	@Schema(description = "유저 상태", example = "ACTIVE") UserState state,

	@Schema(description = "학적 상태", example = "ENROLLED") AcademicStatus academicStatus,

	@Schema(description = "가입 일시", example = "2024-03-01T12:34:56") LocalDateTime createdAt) {
}
