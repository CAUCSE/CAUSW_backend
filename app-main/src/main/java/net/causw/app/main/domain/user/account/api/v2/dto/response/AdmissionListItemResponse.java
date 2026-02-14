package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 재학인증 신청 목록 응답 아이템")
public record AdmissionListItemResponse(

	@Schema(description = "신청 ID") String id,

	@Schema(description = "사용자 이름") String userName,

	@Schema(description = "사용자 이메일") String userEmail,

	@Schema(description = "학과(부)") Department department,

	@Schema(description = "입학년도") Integer admissionYear,

	@Schema(description = "학번") String studentId,

	@Schema(description = "재학 분류") AcademicStatus targetAcademicStatus,

	@Schema(description = "사용자 상태") UserState userState,

	@Schema(description = "신청 시각") LocalDateTime createdAt) {
}
