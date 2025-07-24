package net.causw.app.main.dto.locker;

import net.causw.app.main.domain.model.entity.locker.LockerLocation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LockerLocationResponseDto {
	private String id;
	private String name;
	private Long enableLockerCount;
	private Long totalLockerCount;

	public static LockerLocationResponseDto of(
		LockerLocation lockerLocation,
		Long enableLockerCount,
		Long totalLockerCount
	) {
		return LockerLocationResponseDto.builder()
			.id(lockerLocation.getId())
			.name(lockerLocation.getName())
			.enableLockerCount(enableLockerCount)
			.totalLockerCount(totalLockerCount)
			.build();
	}
}
