package net.causw.application.dto;

import lombok.Getter;
import net.causw.adapter.persistence.LockerLog;
import net.causw.domain.model.LockerLogAction;

import java.time.LocalDateTime;

@Getter
public class LockerLogDetailDto {
    private Long lockerNumber;
    private String userEmail;
    private String userName;
    private LockerLogAction action;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
