package net.causw.app.main.domain.user.account.api.v2.dto.request;

import java.util.List;

import org.springframework.boot.context.properties.bind.DefaultValue;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.userinfo.SortType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동문 수첩 프로필 리스트 조회 필터")
public record UserInfoListCondition(
	@Schema(description = "학번 범위 시작", example = "1972") Integer admissionYearStart,
	@Schema(description = "학번 범위 끝", example = "2025") Integer admissionYearEnd,
	@Schema(description = "학적 상태", example = "[\"ENROLLED\", \"GRADUATED\"]") List<AcademicStatus> academicStatus,
	@Schema(description = "정렬 기준", example = "ASC") @DefaultValue(value = "ASC") SortType sortType) {
}
