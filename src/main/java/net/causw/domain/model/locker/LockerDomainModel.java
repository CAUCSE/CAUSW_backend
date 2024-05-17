package net.causw.domain.model.locker;

import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.user.UserDomainModel;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class LockerDomainModel {
    private String id;

    @NotNull(message = "사물함 번호가 입력되지 않았습니다.")
    private Long lockerNumber;

    @NotNull(message = "사물함 상태가 입력되지 않았습니다.")
    private Boolean isActive;

    private LocalDateTime expiredAt;

    private LocalDateTime updatedAt;

    private UserDomainModel user;

    @NotNull(message = "사물함 위치가 입력되지 않았습니다.")
    private LockerLocationDomainModel lockerLocation;

    public static LockerDomainModel of(
            Long lockerNumber,
            LockerLocationDomainModel lockerLocation
    ) {
        return LockerDomainModel.builder()
                .lockerNumber(lockerNumber)
                .lockerLocation(lockerLocation)
                .build();
    }

    public static LockerDomainModel of(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime expiredAt,
            LocalDateTime updatedAt,
            UserDomainModel user,
            LockerLocationDomainModel lockerLocation
    ) {
        return LockerDomainModel.builder()
                .id(id)
                .lockerNumber(lockerNumber)
                .isActive(isActive)
                .expiredAt(expiredAt)
                .updatedAt(updatedAt)
                .user(user)
                .lockerLocation(lockerLocation)
                .build();
    }

    public void register(UserDomainModel user, LocalDateTime expiredAt) {
        this.user = user;
        this.isActive = Boolean.FALSE;
        this.expiredAt = expiredAt;
    }

    public void returnLocker() {
        this.user = null;
        this.isActive = Boolean.TRUE;
        this.expiredAt = null;
    }

    public void extendExpireDate(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
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
