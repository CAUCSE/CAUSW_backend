package net.causw.app.main.domain.asset.locker.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LockerStatus {
	AVAILABLE("사용 가능"),
	IN_USE("사용중"),
	DISABLED("비활성");

	private final String description;

	public static LockerStatus of(Locker locker) {
		if (locker.getUser().isPresent()) {
			return IN_USE;
		}
		if (Boolean.TRUE.equals(locker.getIsActive())) {
			return AVAILABLE;
		}
		return DISABLED;
	}
}
