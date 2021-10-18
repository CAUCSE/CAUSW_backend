package net.causw.domain.model;

import lombok.Getter;

@Getter
public class LockerLocationDomainModel {
    private String id;
    private String location;
    private String locationDesc;

    private LockerLocationDomainModel(
            String id,
            String location,
            String locationDesc
    ) {
        this.id = id;
        this.location = location;
        this.locationDesc = locationDesc;
    }

    public static LockerLocationDomainModel of(
            String id,
            String location,
            String locationDesc
    ) {
        return new LockerLocationDomainModel(
                id,
                location,
                locationDesc
        );
    }
}
