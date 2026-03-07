package net.causw.app.main.domain.asset.locker.api.v2.controller.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 사물함 조회 응답")
public record MyLockerResponse(

	@Schema(description = "사물함 보유 여부", example = "true") boolean hasLocker,

	@Schema(description = "사물함 ID", example = "locker-uuid-1234", nullable = true) String lockerId,

	@Schema(description = "사물함 위치 표시명", example = "4층 15번", nullable = true) String displayName,

	@Schema(description = "만료일시", example = "2026-06-30T23:59:59", nullable = true) LocalDateTime expiredAt) {

	public static MyLockerResponse empty() {
		return new MyLockerResponse(false, null, null, null);
	}

	public static MyLockerResponse of(String lockerId, String displayName, LocalDateTime expiredAt) {
		return new MyLockerResponse(true, lockerId, displayName, expiredAt);
	}
}
