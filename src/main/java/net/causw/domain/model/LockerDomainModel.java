package net.causw.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LockerDomainModel {
    private String id;
    private Long lockerNumber;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String userId;
    private String userName;
    private LockerLocationDomainModel lockerLocation;

    private LockerDomainModel(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updatedAt,
            String userId,
            String userName,
            LockerLocationDomainModel lockerLocation
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.userName = userName;
        this.lockerLocation = lockerLocation;
    }

    public static LockerDomainModel of(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updatedAt,
            String userId,
            String userName,
            LockerLocationDomainModel lockerLocation
    ) {
        return new LockerDomainModel(
                id,
                lockerNumber,
                isActive,
                updatedAt,
                userId,
                userName,
                lockerLocation
        );
    }
}
