package net.causw.app.main.domain.asset.locker.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LockerStatus {
	AVAILABLE("사용 가능"),
	MINE("내 사물함"),
	IN_USE("사용중"),
	DISABLED("비활성");

	private final String description;

	static LockerStatus of(Locker locker) {
		if (locker.getUser().isPresent()) {
			return IN_USE;
		}
		if (Boolean.TRUE.equals(locker.getIsActive())) {
			return AVAILABLE;
		}
		return DISABLED;
	}

	static LockerStatus of(Locker locker, String userId) {
		LockerStatus base = of(locker);
		if (base == IN_USE
			&& locker.getUser().map(u -> u.getId().equals(userId)).orElse(false)) {
			return MINE;
		}
		return base;
	}
}
