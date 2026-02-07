package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학적 변경 신청 목록 조회 요청")
public record AcademicRecordApplicationListRequest(
	@Schema(description = "신청 상태 필터 (AWAIT, ACCEPT, REJECT, CLOSE)", example = "AWAIT") AcademicRecordRequestStatus requestStatus,

	@Schema(description = "학과 필터") Department department,

	@Schema(description = "검색 키워드 (이름 또는 학번)") String keyword,

	@Schema(description = "페이지 번호 (0부터 시작)", example = "0") Integer page,

	@Schema(description = "페이지 크기", example = "10") Integer size) {
}
