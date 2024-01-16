package net.causw.application.dto.locker;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.locker.LockerLocationDomainModel;

@Getter
@Setter
public class LockerLocationResponseDto {
    private String id;
    private String name;
    private Long enableLockerCount;
    private Long totalLockerCount;

    private LockerLocationResponseDto(
            String id,
            String name,
            Long enableLockerCount,
            Long totalLockerCount
    ) {
        this.id = id;
        this.name = name;
        this.enableLockerCount = enableLockerCount;
        this.totalLockerCount = totalLockerCount;
    }

    public static LockerLocationResponseDto from(
            LockerLocationDomainModel lockerLocation,
            Long enableLockerCount,
            Long totalLockerCount
    ) {
        return new LockerLocationResponseDto(
                lockerLocation.getId(),
                lockerLocation.getName(),
                enableLockerCount,
                totalLockerCount
        );
    }

}
