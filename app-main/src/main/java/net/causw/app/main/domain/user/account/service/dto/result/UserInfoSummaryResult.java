package net.causw.app.main.domain.user.account.service.dto.result;

import net.causw.app.main.shared.dto.ProfileImageDto;

public record UserInfoSummaryResult(
	String id,
	ProfileImageDto profileImage,
	String name,
	String admissionYear,
	String academicStatus,
	String description) {
}
