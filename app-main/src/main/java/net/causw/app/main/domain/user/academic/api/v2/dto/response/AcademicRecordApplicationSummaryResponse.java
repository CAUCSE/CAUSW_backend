package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학적 변경 신청 요약 응답 (목록 조회용)")
public record AcademicRecordApplicationSummaryResponse(
	@Schema(description = "신청서 ID") String applicationId,
	@Schema(description = "신청자 ID") String userId,
	@Schema(description = "신청자 이름") String userName,
	@Schema(description = "신청자 학번") String studentId,
	@Schema(description = "신청자 학과") Department department,
	@Schema(description = "현재 학적 상태") AcademicStatus currentAcademicStatus,
	@Schema(description = "변경 요청 학적 상태") AcademicStatus targetAcademicStatus,
	@Schema(description = "신청 처리 상태") AcademicRecordRequestStatus requestStatus,
	@Schema(description = "신청 일시") LocalDateTime createdAt) {
}
