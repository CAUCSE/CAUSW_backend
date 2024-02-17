package net.causw.application.dto.locker;

import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.domain.model.enums.LockerLogAction;

import java.time.LocalDateTime;

@Getter
@Setter
public class LockerLogResponseDto {
    private final Long lockerNumber;
    private final String userEmail;
    private final String userName;
    private final LockerLogAction action;
    private final String message;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LockerLogResponseDto(
            Long lockerNumber,
            String userEmail,
            String userName,
            LockerLogAction action,
            String message,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.lockerNumber = lockerNumber;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LockerLogResponseDto from(LockerLog lockerLog) {
        return new LockerLogResponseDto(
                lockerLog.getLockerNumber(),
                lockerLog.getUserEmail(),
                lockerLog.getUserName(),
                lockerLog.getAction(),
                lockerLog.getMessage(),
                lockerLog.getCreatedAt(),
                lockerLog.getUpdatedAt()
        );
    }
}
