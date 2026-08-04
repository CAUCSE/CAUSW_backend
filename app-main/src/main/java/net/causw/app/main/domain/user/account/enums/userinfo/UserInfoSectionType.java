package net.causw.app.main.domain.user.account.enums.userinfo;

public enum UserInfoSectionType {
	COFFEE_CHAT_AVAILABLE,
	ALL_MEMBERS;

	public UserInfoSectionType next() {
		return switch (this) {
			case COFFEE_CHAT_AVAILABLE -> ALL_MEMBERS;
			case ALL_MEMBERS -> null;
		};

	}
}
