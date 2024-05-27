package net.causw.application.dto.locker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.domain.model.enums.LockerLogAction;

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
