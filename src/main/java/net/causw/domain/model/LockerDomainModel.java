package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
public class LockerDomainModel {
    private String id;

    @NotNull(message = "사물함 번호가 입력되지 않았습니다.")
    private Long lockerNumber;

    @NotNull(message = "사물함 상태가 입력되지 않았습니다.")
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private UserDomainModel user;

    @NotNull(message = "사물함 위치가 입력되지 않았습니다.")
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

    public void register(UserDomainModel user) {
        this.user = user;
        this.isActive = Boolean.FALSE;
    }

    public void returnLocker() {
        this.user = null;
        this.isActive = Boolean.TRUE;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
        this.user = null;
    }

    public void move(LockerLocationDomainModel lockerLocation) {
        this.lockerLocation = lockerLocation;
    }

    public Optional<UserDomainModel> getUser() {
        return Optional.ofNullable(this.user);
    }
}
