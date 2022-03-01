package net.causw.application.dto.locker;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LockersResponseDto {
    private String locationName;
    private List<LockerResponseDto> lockerList;

    private LockersResponseDto(String locationName, List<LockerResponseDto> lockerList) {
        this.locationName = locationName;
        this.lockerList = lockerList;
    }

    public static LockersResponseDto of(String locationName, List<LockerResponseDto> lockerList) {
        return new LockersResponseDto(locationName, lockerList);
    }
}
