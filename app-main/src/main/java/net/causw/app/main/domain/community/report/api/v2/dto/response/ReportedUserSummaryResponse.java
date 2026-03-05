package net.causw.app.main.domain.community.report.api.v2.dto.response;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 신고 회원 목록 아이템")
public record ReportedUserSummaryResponse(

	@Schema(description = "회원 ID", example = "11112222-aaaa-3333-bbbb-446655440000") String userId,

	@Schema(description = "학번", example = "20201234") String studentId,

	@Schema(description = "이름", example = "홍길동") String name,

	@Schema(description = "학적 상태", example = "ENROLLED") AcademicStatus academicStatus,

	@Schema(description = "신고 누적 횟수", example = "3") Integer reportedCount,

	@Schema(description = "회원 상태", example = "ACTIVE") UserState userState) {
}
