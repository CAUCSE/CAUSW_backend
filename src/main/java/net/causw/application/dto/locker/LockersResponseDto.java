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
    private List<LockerResponseDto> lockerList;

    public static LockersResponseDto of(String locationName, List<LockerResponseDto> lockerList) {
        return LockersResponseDto.builder()
                .locationName(locationName)
                .lockerList(lockerList)
                .build();
    }
}
