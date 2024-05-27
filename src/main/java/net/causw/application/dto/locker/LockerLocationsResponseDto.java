package net.causw.application.dto.locker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
