package net.causw.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
public class LockerDomainModel {
    private String id;
    private Long lockerNumber;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private UserDomainModel user;
    private LockerLocationDomainModel lockerLocation;

    private LockerDomainModel(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updatedAt,
            UserDomainModel user,
            LockerLocationDomainModel lockerLocation
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.updatedAt = updatedAt;
        this.user = user;
        this.lockerLocation = lockerLocation;
    }

    public static LockerDomainModel of(
            Long lockerNumber,
            LockerLocationDomainModel lockerLocation
    ) {
        return new LockerDomainModel(
                null,
                lockerNumber,
                true,
                null,
                null,
                lockerLocation
        );
    }

    public static LockerDomainModel of(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updatedAt,
            UserDomainModel user,
            LockerLocationDomainModel lockerLocation
    ) {
        return new LockerDomainModel(
                id,
                lockerNumber,
                isActive,
                updatedAt,
                user,
                lockerLocation
        );
    }

    public Optional<UserDomainModel> getUser() {
        return Optional.ofNullable(this.user);
    }
}
