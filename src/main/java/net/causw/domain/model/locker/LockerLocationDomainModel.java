package net.causw.domain.model.locker;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class LockerLocationDomainModel {
    private String id;

    @NotBlank(message = "사물함 위치명이 입력되지 않았습니다.")
    private String name;

    public static LockerLocationDomainModel of(
            String id,
            String name
    ) {
        return LockerLocationDomainModel.builder()
                .id(id)
                .name(name)
                .build();
    }

    public static LockerLocationDomainModel from(String name) {
        return LockerLocationDomainModel.builder()
                .name(name)
                .build();
    }

    public void update(String name) {
        this.name = name;
    }
}
