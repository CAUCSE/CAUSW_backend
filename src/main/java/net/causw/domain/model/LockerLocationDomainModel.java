package net.causw.domain.model;

import lombok.Getter;

@Getter
public class LockerLocationDomainModel {
    private String id;
    private String name;
    private String description;

    private LockerLocationDomainModel(
            String id,
            String name,
            String description
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static LockerLocationDomainModel of(
            String id,
            String name,
            String description
    ) {
        return new LockerLocationDomainModel(
                id,
                name,
                description
        );
    }

    public static LockerLocationDomainModel of(
            String name,
            String description
    ) {
        return new LockerLocationDomainModel(
                null,
                name,
                description
        );
    }
}
