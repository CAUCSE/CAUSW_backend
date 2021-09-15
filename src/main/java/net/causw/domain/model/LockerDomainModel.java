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

    private LockerDomainModel(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updatedAt,
            String userId,
            String userName
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
        this.userId = userId;
        this.userName = userName;
    }

    public static LockerDomainModel of(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updatedAt,
            String userId,
            String userName
    ) {
        return new LockerDomainModel(
                id,
                lockerNumber,
                isActive,
                updatedAt,
                userId,
                userName
        );
    }
}
