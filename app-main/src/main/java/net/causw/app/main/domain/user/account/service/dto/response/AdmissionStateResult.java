package net.causw.app.main.domain.user.account.service.dto.response;

import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record AdmissionStateResult(
	UserState userState,
	boolean hasAdmission,
	String rejectReason) {

	public static AdmissionStateResult of(User user, boolean hasAdmission) {
		return new AdmissionStateResult(
			user.getState(),
			hasAdmission,
			user.getRejectionOrDropReason());
	}
}
