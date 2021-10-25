package net.causw.application.dto;

import lombok.Getter;
import net.causw.adapter.persistence.Locker;
import net.causw.domain.model.LockerDomainModel;

import java.time.LocalDateTime;

@Getter
public class LockerResponseDto {
    private String id;
    private Long lockerNumber;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private String userId;
    private String userName;
    private String lockerLocationName; // TODO : 추후 locker 디자인 확정 이후 유지/삭제 결정

    private LockerResponseDto(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updateAt,
            String userId,
            String userName,
            String lockerLocationName
    ){
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.updatedAt = updateAt;
        this.userId = userId;
        this.userName = userName;
        this.lockerLocationName = lockerLocationName;
    }

    public static LockerResponseDto from(Locker locker) {
        return new LockerResponseDto(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUpdatedAt(),
                locker.getUser().getId(),
                locker.getUser().getName(),
                locker.getLocation().getName()
        );
    }

    public static LockerResponseDto from(LockerDomainModel locker) {
        return new LockerResponseDto(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUpdatedAt(),
                locker.getUserId(),
                locker.getUserName(),
                locker.getLockerLocation().getName()
        );
    }
}
