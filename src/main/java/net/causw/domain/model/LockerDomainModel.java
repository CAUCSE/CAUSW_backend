package net.causw.domain.model;

import lombok.Getter;

@Getter
public class LockerDomainModel {
    private String id;
    private Long lockerNumber;
    private Boolean isActive;
    private UserDomainModel user;

    private LockerDomainModel(
            String id,
            Long lockerNumber,
            Boolean isActive,
            UserDomainModel user
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.user = user;
    }

    public static LockerDomainModel of(
            String id,
            Long lockerNumber,
            Boolean isActive,
            UserDomainModel user
    ) {
        return new LockerDomainModel(
                id,
                lockerNumber,
                isActive,
                user
        );
    }
}
