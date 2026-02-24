package net.causw.app.main.domain.user.account.api.v2.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserProjectDto(
	String id,

	Integer startYear,

	Integer startMonth,

	Integer endYear,

	Integer endMonth,

	@NotNull @Size(max = 50, message = "최대 글자 수 50을 초과했습니다.") String description) {
}
