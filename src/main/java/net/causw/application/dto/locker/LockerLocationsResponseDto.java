package net.causw.application.dto.locker;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LockerLocationsResponseDto {
    private List<LockerLocationResponseDto> lockerLocations;
    private LockerResponseDto myLocker;

    private LockerLocationsResponseDto(
            List<LockerLocationResponseDto> lockerLocations,
            LockerResponseDto myLocker
    ) {
        this.lockerLocations = lockerLocations;
        this.myLocker = myLocker;
    }

    public static LockerLocationsResponseDto of(
            List<LockerLocationResponseDto> lockerLocations,
            LockerResponseDto myLocker
    ) {
        return new LockerLocationsResponseDto(lockerLocations, myLocker);
    }
}
