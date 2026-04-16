package net.causw.app.main.domain.user.account.api.v2.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.DeletedUserSortType;
import net.causw.app.main.domain.user.account.enums.user.Department;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeletedUserSearchCondition(
	@Schema(description = "키워드 (이메일·이름·학번 like 검색)") String keyword,
	@Schema(description = "학과") Department department,
	@Schema(description = "입학년도 시작 (포함)") Integer admissionYearFrom,
	@Schema(description = "입학년도 끝 (포함)") Integer admissionYearTo,
	@Schema(description = "학적 상태") AcademicStatus academicStatus,
	@Schema(description = "정렬 기준 (기본: DELETED_AT_DESC)") DeletedUserSortType sortBy) {
}
