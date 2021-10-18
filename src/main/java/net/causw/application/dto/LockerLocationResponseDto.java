package net.causw.application.dto;

import net.causw.domain.model.LockerLocationDomainModel;

public class LockerLocationResponseDto {
    private String id;
    private String location;
    private String locationDesc;

    private LockerLocationResponseDto(
            String id,
            String location,
            String locationDesc
    ) {
        this.id = id;
        this.location = location;
        this.locationDesc = locationDesc;
    }

    public static LockerLocationResponseDto from(LockerLocationDomainModel lockerLocation) {
        return new LockerLocationResponseDto(
                lockerLocation.getId(),
                lockerLocation.getLocation(),
                lockerLocation.getLocationDesc()
        );
    }

}
