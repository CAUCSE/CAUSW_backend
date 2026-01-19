package net.causw.app.main.domain.asset.locker.api.v1.dto;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.locker.entity.LockerLog;
import net.causw.app.main.domain.asset.locker.enums.LockerLogAction;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LockerLogResponseDto {
	private final Long lockerNumber;
	private final String userEmail;
	private final String userName;
	private final LockerLogAction action;
	private final String message;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;

	public static LockerLogResponseDto from(LockerLog lockerLog) {
		return LockerLogResponseDto.builder()
			.lockerNumber(lockerLog.getLockerNumber())
			.userEmail(lockerLog.getUserEmail())
			.userName(lockerLog.getUserName())
			.action(lockerLog.getAction())
			.message(lockerLog.getMessage())
			.createdAt(lockerLog.getCreatedAt())
			.updatedAt(lockerLog.getUpdatedAt())
			.build();
	}
}
