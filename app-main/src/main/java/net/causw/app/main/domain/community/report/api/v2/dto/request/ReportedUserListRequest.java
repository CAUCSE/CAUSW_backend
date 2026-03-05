package net.causw.app.main.domain.community.report.api.v2.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 신고 회원 목록 조회 요청")
public record ReportedUserListRequest(

	@Schema(description = "이름 또는 학번 검색 키워드", example = "홍길동") String keyword,

	@Schema(description = "회원 상태 필터", example = "ACTIVE") UserState state,

	@Schema(description = "학적 상태 필터", example = "ENROLLED") AcademicStatus academicStatus) {
}
