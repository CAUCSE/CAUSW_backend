package net.causw.application.dto;

import lombok.Getter;
import net.causw.adapter.persistence.Locker;

import java.time.LocalDateTime;

@Getter
public class LockerDetailDto {
    private String id;
    private Long lockerNumber;
    private Boolean isActive;
    private LocalDateTime updatedAt;
    private UserResponseDto user;

    private LockerDetailDto(
            String id,
            Long lockerNumber,
            Boolean isActive,
            LocalDateTime updateAt,
            UserResponseDto user
    ){
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.isActive = isActive;
        this.updatedAt = updateAt;
        this.user = user;
    }

    public static LockerDetailDto from(Locker locker) {
        return new LockerDetailDto(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUpdatedAt(),
                UserResponseDto.from(locker.getUser())
        );
    }
}
