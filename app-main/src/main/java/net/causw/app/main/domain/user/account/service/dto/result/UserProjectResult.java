package net.causw.app.main.domain.user.account.service.dto.result;

public record UserProjectResult(
	String id,
	int startYear,
	int startMonth,
	Integer endYear,
	Integer endMonth,
	String description) {
}
