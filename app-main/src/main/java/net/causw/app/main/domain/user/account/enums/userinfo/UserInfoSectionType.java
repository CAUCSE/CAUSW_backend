package net.causw.app.main.domain.user.account.enums.userinfo;

public enum UserInfoSectionType {
	COFFEE_CHAT_AVAILABLE,
	ALL_MEMBERS;

	/**
	 * 현재 섹션 다음에 조회할 섹션을 반환한다.
	 * @return 다음 섹션, 마지막 섹션인 경우 {@code null}
	 */
	public UserInfoSectionType next() {
		return switch (this) {
			case COFFEE_CHAT_AVAILABLE -> ALL_MEMBERS;
			case ALL_MEMBERS -> null;
		};

	}
}
