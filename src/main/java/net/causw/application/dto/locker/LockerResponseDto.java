package net.causw.application.dto.locker;

import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.Locker;
import net.causw.adapter.persistence.User;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;

@Getter
@Setter
public class LockerResponseDto {
    private String id;
    private String lockerNumber;
    private Boolean isActive;
    private Boolean isMine;
    private LocalDateTime updatedAt;

    private LockerResponseDto(
            String id,
            String lockerNumber,
            Boolean isActive,
            Boolean isMine,
            LocalDateTime updateAt
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.isMine = isMine;
        this.updatedAt = updateAt;
    }

    public static LockerResponseDto from(Locker locker, UserDomainModel user) {
        return new LockerResponseDto(
                locker.getId(),
                String.valueOf(locker.getLockerNumber()),
                locker.getIsActive(),
                locker.getUser().map(User::getId).orElse("").equals(user.getId()),
                locker.getUpdatedAt()
        );
    }

    public static LockerResponseDto from(LockerDomainModel locker, UserDomainModel user) {
        return new LockerResponseDto(
                locker.getId(),
                String.valueOf(locker.getLockerNumber()),
                locker.getIsActive(),
                locker.getUser().map(UserDomainModel::getId).orElse("").equals(user.getId()),
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
                locker.getUpdatedAt()
        );
    }
}
