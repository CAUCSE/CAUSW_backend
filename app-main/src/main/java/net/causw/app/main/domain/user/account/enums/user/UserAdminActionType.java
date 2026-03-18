package net.causw.app.main.domain.user.account.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAdminActionType {
	DROP("유저 추방"),
	RESTORE("추방 유저 복구"),
	ROLE_CHANGE("유저 역할 변경");

	private final String description;
}
