package net.causw.app.main.domain.user.account.service.v2.dto;

import net.causw.app.main.domain.user.account.enums.user.UserState;

public record AdmissionListCondition(
	String keyword,
	UserState userState) {
}
