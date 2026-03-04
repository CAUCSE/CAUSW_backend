package net.causw.app.main.domain.user.account.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAdminActionType {
	DROP("drop"),
	RESTORE("restore"),
	ROLE_CHANGE("role_change");

	private final String value;
}
