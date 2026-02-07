package net.causw.app.main.domain.user.academic.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "학적 변경 신청 상세 응답")
public record AcademicRecordApplicationDetailResponse(
		@Schema(description = "신청서 ID") String applicationId,
		@Schema(description = "신청자 ID") String userId,
		@Schema(description = "신청자 이름") String userName,
		@Schema(description = "신청자 학번") String studentId,
		@Schema(description = "신청자 학과") Department department,
		@Schema(description = "현재 학적 상태") AcademicStatus currentAcademicStatus,
		@Schema(description = "변경 요청 학적 상태") AcademicStatus targetAcademicStatus,
		@Schema(description = "변경 요청 이수 학기") Integer targetCompletedSemester,
		@Schema(description = "신청 처리 상태") AcademicRecordRequestStatus requestStatus,
		@Schema(description = "신청자 메모") String note,
		@Schema(description = "반려 사유") String rejectMessage,
		@Schema(description = "첨부 이미지 URL 목록") List<String> attachImageUrls,
		@Schema(description = "신청 일시") LocalDateTime createdAt,
		@Schema(description = "최종 수정 일시") LocalDateTime updatedAt
) {
}
