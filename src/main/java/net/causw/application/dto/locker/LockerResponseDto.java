package net.causw.application.dto.locker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Getter
@Setter
@Builder
public class LockerResponseDto {
    private String id;
    private String lockerNumber;
    private Boolean isActive;
    private Boolean isMine;
    private String expireAt;
    private LocalDateTime updatedAt;

    public static LockerResponseDto of(Locker locker, User user) {
        return LockerResponseDto.builder()
                .id(locker.getId())
                .lockerNumber(String.valueOf(locker.getLockerNumber()))
                .isActive(locker.getIsActive())
                .isMine(locker.getUser().map(User::getId).orElse("").equals(user.getId()))
                .expireAt(Optional.ofNullable(locker.getExpireDate()).map(
                        expire -> expire.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))).orElse(null))
                .updatedAt(locker.getUpdatedAt())
                .build();
    }

    public static LockerResponseDto of(
            Locker locker,
            User user,
            String locationName
    ) {
        String location = locationName + " " + locker.getLockerNumber();

        return LockerResponseDto.builder()
                .id(locker.getId())
                .lockerNumber(location)
                .isActive(locker.getIsActive())
                .isMine(locker.getUser().map(User::getId).orElse("").equals(user.getId()))
                .expireAt(Optional.ofNullable(locker.getExpireDate()).map(
                        expire -> expire.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))).orElse(null))
                .updatedAt(locker.getUpdatedAt())
                .build();
    }
}
