package net.causw.app.main.domain.user.account.service.dto.result;

import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.shared.dto.ProfileImageDto;

public record UserInfoSummaryResult(
	String id,
	ProfileImageDto profileImage,
	String name,
	String admissionYear,
	String academicStatus,
	Department department,
	String description,
	boolean isCoffeeChatAvailable) {
}
