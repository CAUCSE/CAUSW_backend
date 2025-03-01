package net.causw.application.dto.locker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
