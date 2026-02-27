package net.causw.app.main.domain.user.account.service.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동문 수첩 프로필 리스트 조회 필터")
public record UserInfoListCondition(
	@Schema(description = "검색 키워드 (이름, 직업, 경력)", example = "검색어") String keyword,
	@Schema(description = "학번 범위 시작", example = "1972") Integer admissionYearStart,
	@Schema(description = "학번 범위 끝", example = "2025") Integer admissionYearEnd,
	@Schema(description = "학적 상태", example = "[\"ENROLLED\", \"GRADUATED\"]") List<String> academicStatus,
	@Schema(description = "정렬 기준", example = "UPDATED_AT_DESC") String sortType) {
}
