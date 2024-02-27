package net.causw.application.dto.locker;

import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.locker.LockerDomainModel;
import net.causw.domain.model.user.UserDomainModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Getter
@Setter
public class LockerResponseDto {
    private String id;
    private String lockerNumber;
    private Boolean isActive;
    private Boolean isMine;
    private String expireAt;
    private LocalDateTime updatedAt;

    private LockerResponseDto(
            String id,
            String lockerNumber,
            Boolean isActive,
            Boolean isMine,
            String expireAt,
            LocalDateTime updateAt
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.isMine = isMine;
        this.expireAt = expireAt;
        this.updatedAt = updateAt;
    }

    public static LockerResponseDto from(Locker locker, UserDomainModel user) {
        return new LockerResponseDto(
                locker.getId(),
                String.valueOf(locker.getLockerNumber()),
                locker.getIsActive(),
                locker.getUser().map(User::getId).orElse("").equals(user.getId()),
                Optional.ofNullable(locker.getExpireDate()).map(
                        expire -> expire.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).orElse(null),
                locker.getUpdatedAt()
        );
    }

    public static LockerResponseDto from(LockerDomainModel locker, UserDomainModel user) {
        return new LockerResponseDto(
                locker.getId(),
                String.valueOf(locker.getLockerNumber()),
                locker.getIsActive(),
                locker.getUser().map(UserDomainModel::getId).orElse("").equals(user.getId()),
                Optional.ofNullable(locker.getExpiredAt()).map(
                        expire -> expire.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).orElse(null),
                locker.getUpdatedAt()
        );
    }

    public static LockerResponseDto from(
            LockerDomainModel locker,
            UserDomainModel user,
            String locationName
    ) {
        String location = locationName + " " + locker.getLockerNumber();

        return new LockerResponseDto(
                locker.getId(),
                location,
                locker.getIsActive(),
                locker.getUser().map(UserDomainModel::getId).orElse("").equals(user.getId()),
                Optional.ofNullable(locker.getExpiredAt()).map(
                        expire -> expire.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).orElse(null),
                locker.getUpdatedAt()
        );
    }
}
