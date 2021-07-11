package net.causw.domain.model;

import lombok.Getter;
import net.causw.infra.LockerLog;
import net.causw.infra.LockerType;

@Getter
public class LockerLogDomainModel {
    private String id;
    private Long lockerNumber;
    private String userEmail;
    private LockerType type;

    private LockerLogDomainModel(
            String id,
            Long lockerNumber,
            String userEmail,
            LockerType type
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.userEmail = userEmail;
        this.type = type;
    }

    public static LockerLogDomainModel of(LockerLog log) {
        return new LockerLogDomainModel(
                log.getId(),
                log.getLockerNumber(),
                log.getUserEmail(),
                log.getLockerType()
        );
    }
}
