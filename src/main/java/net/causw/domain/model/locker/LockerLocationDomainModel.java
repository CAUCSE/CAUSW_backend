package net.causw.domain.model.locker;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class LockerLocationDomainModel {
    private String id;

    @NotBlank(message = "사물함 위치명이 입력되지 않았습니다.")
    private String name;

    private LockerLocationDomainModel(
            String id,
            String name
    ) {
        this.id = id;
        this.name = name;
    }

    public static LockerLocationDomainModel of(
            String id,
            String name
    ) {
        return new LockerLocationDomainModel(
                id,
                name
        );
    }

    public static LockerLocationDomainModel of(String name) {
        return new LockerLocationDomainModel(
                null,
                name
        );
    }

    public void update(String name) {
        this.name = name;
    }
}
