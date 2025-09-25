package net.causw.app.main.dto.userInfo;

import java.util.List;

import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserInfoSearchConditionDto(

	@Schema(description = "검색어(이름, 직업, 경력)", example = "홍길동")
	String keyword,
	@Schema(description = "입학 년도 검색 구간 하방", example = "1990")
	Integer admissionYearStart,
	@Schema(description = "입학 년도 검색 구간 상방", example = "2020")
	Integer admissionYearEnd,
	@Schema(description = "학적 상태(ENROLLED, LEAVE_OF_ABSENCE, GRADUATED, 그외 등등)", example = "ENROLLED")
	List<AcademicStatus> academicStatus
	) {
}
