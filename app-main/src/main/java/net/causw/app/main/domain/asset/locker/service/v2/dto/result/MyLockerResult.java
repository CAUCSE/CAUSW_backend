package net.causw.app.main.domain.asset.locker.service.v2.dto.result;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record MyLockerResult(
	boolean hasLocker,
	String lockerId,
	String displayName,
	LocalDateTime expiredAt) {

	public static MyLockerResult empty() {
		return new MyLockerResult(false, null, null, null);
	}

	public static MyLockerResult of(String lockerId, String displayName, LocalDateTime expiredAt) {
		return new MyLockerResult(true, lockerId, displayName, expiredAt);
	}
}
