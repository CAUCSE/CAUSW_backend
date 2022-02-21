package net.causw.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LockerAllLocationResponseDto {
    private List<LockerLocationResponseDto> lockerLocations;
    private LockerResponseDto myLocker;

    private LockerAllLocationResponseDto(
            List<LockerLocationResponseDto> lockerLocations,
            LockerResponseDto myLocker
    ) {
        this.lockerLocations = lockerLocations;
        this.myLocker = myLocker;
    }

    public static LockerAllLocationResponseDto of(
            List<LockerLocationResponseDto> lockerLocations,
            LockerResponseDto myLocker
    ) {
        return new LockerAllLocationResponseDto(lockerLocations, myLocker);
    }
}
