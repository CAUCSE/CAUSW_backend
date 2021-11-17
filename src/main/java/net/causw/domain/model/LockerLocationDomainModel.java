package net.causw.domain.model;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class LockerLocationDomainModel {
    private String id;

    @NotBlank(message = "사물함 위치명이 입력되지 않았습니다.")
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
