package net.causw.app.main.domain.user.account.service.dto.result;

public record UserInfoSummaryResult(
	String id,
	String profileImageUrl,
	String name,
	String admissionYear,
	String academicStatus,
	String job,
	String description) {
}
