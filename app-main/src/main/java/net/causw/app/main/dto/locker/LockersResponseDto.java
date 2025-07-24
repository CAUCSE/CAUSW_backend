package net.causw.app.main.dto.locker;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LockersResponseDto {
	private String locationName;
	private String lockerPeriod;
	private List<LockerResponseDto> lockerList;

	public static LockersResponseDto of(String locationName, String lockerPeriod, List<LockerResponseDto> lockerList) {
		return LockersResponseDto.builder()
			.locationName(locationName)
			.lockerPeriod(lockerPeriod)
			.lockerList(lockerList)
			.build();
	}
}
