package net.causw.application.dto;

import net.causw.domain.model.LockerLocationDomainModel;

public class LockerLocationResponseDto {
    private String id;
    private String name;
    private String description;
    private Long enableLockerCount;
    private Long totalLockerCount;

    private LockerLocationResponseDto(
            String id,
            String name,
            String description,
            Long enableLockerCount,
            Long totalLockerCount
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
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
                lockerLocation.getDescription(),
                enableLockerCount,
                totalLockerCount
        );
    }

}
