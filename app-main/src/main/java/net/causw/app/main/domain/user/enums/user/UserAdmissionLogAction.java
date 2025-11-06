package net.causw.app.main.domain.user.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserAdmissionLogAction {
	ACCEPT("accept"),
	REJECT("reject");

	private final String value;

}
