package net.causw.app.main.domain.user.account.service.dto.request;

import net.causw.app.main.domain.user.account.enums.user.UserState;

public record AdmissionListCondition(
	String keyword,
	UserState userState) {
}
