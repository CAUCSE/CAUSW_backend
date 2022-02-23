package net.causw.application.dto;

import lombok.Getter;
import net.causw.adapter.persistence.LockerLog;
import net.causw.domain.model.LockerLogAction;

import java.time.LocalDateTime;

@Getter
public class LockerLogDetailDto {
    private final Long lockerNumber;
    private final String userEmail;
    private final String userName;
    private final LockerLogAction action;
    private final String message;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LockerLogDetailDto(
            Long lockerNumber,
            String userEmail,
            String userName,
            LockerLogAction action,
            String message,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){
        this.lockerNumber = lockerNumber;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LockerLogDetailDto from(LockerLog lockerLog) {
        return new LockerLogDetailDto(
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
