package net.causw.app.main.dto.locker;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LockerLocationsResponseDto {
	private List<LockerLocationResponseDto> lockerLocations;
	private LockerResponseDto myLocker;

	public static LockerLocationsResponseDto of(
		List<LockerLocationResponseDto> lockerLocations,
		LockerResponseDto myLocker
	) {
		return LockerLocationsResponseDto.builder()
			.lockerLocations(lockerLocations)
			.myLocker(myLocker)
			.build();
	}
}
