package net.causw.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LockerAllResponseDto {
    private String locationName;
    private List<LockerResponseDto> lockerList;

    private LockerAllResponseDto(String locationName, List<LockerResponseDto> lockerList) {
        this.locationName = locationName;
        this.lockerList = lockerList;
    }

    public static LockerAllResponseDto of(String locationName, List<LockerResponseDto> lockerList) {
        return new LockerAllResponseDto(locationName, lockerList);
    }
}
