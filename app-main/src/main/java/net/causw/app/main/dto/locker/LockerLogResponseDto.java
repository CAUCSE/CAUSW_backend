package net.causw.app.main.dto.locker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.app.main.domain.model.entity.locker.LockerLog;
import net.causw.app.main.domain.model.enums.locker.LockerLogAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LockerLogResponseDto {
    private final Long lockerNumber;
    private final String userEmail;
    private final String userName;
    private final LockerLogAction action;
    private final String message;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static LockerLogResponseDto from(LockerLog lockerLog) {
        return LockerLogResponseDto.builder()
                .lockerNumber(lockerLog.getLockerNumber())
                .userEmail(lockerLog.getUserEmail())
                .userName(lockerLog.getUserName())
                .action(lockerLog.getAction())
                .message(lockerLog.getMessage())
                .createdAt(lockerLog.getCreatedAt())
                .updatedAt(lockerLog.getUpdatedAt())
                .build();
    }
}
