package net.causw.application.dto;

import lombok.Getter;
import net.causw.adapter.persistence.Locker;
import net.causw.adapter.persistence.User;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;

@Getter
public class LockerResponseDto {
    private String id;
    private Long lockerNumber;
    private Boolean isActive;
    private Boolean isMine;
    private LocalDateTime updatedAt;
    private String userId;
    private String userName;
    private String lockerLocationName; // TODO : 추후 locker 디자인 확정 이후 유지/삭제 결정

    private LockerResponseDto(
            String id,
            Long lockerNumber,
            Boolean isActive,
            Boolean isMine,
            LocalDateTime updateAt,
            String userId,
            String userName,
            String lockerLocationName
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.isMine = isMine;
        this.updatedAt = updateAt;
        this.userId = userId;
        this.userName = userName;
        this.lockerLocationName = lockerLocationName;
    }

    public static LockerResponseDto from(Locker locker, UserDomainModel user) {
        return new LockerResponseDto(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUser().map(User::getId).orElse("").equals(user.getId()),
                locker.getUpdatedAt(),
                locker.getUser().map(User::getId).orElse(null),
                locker.getUser().map(User::getName).orElse(null),
                locker.getLocation().getName()
        );
    }

    public static LockerResponseDto from(LockerDomainModel locker, UserDomainModel user) {
        return new LockerResponseDto(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUser().map(UserDomainModel::getId).orElse("").equals(user.getId()),
                locker.getUpdatedAt(),
                locker.getUser().map(UserDomainModel::getId).orElse(null),
                locker.getUser().map(UserDomainModel::getName).orElse(null),
                locker.getLockerLocation().getName()
        );
    }
}
