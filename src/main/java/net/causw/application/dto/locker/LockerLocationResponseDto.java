package net.causw.application.dto.locker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.locker.LockerLocation;

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
