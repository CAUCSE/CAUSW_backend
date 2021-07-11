package net.causw.domain.model;

import lombok.Getter;
import net.causw.infra.LockerLog;
import net.causw.infra.LockerType;

@Getter
public class LockerLogDomainModel {
    private String id;
    private LockerType type;
    private LockerDomainModel locker;
    private UserDomainModel user;

    private LockerLogDomainModel(
            String id,
            LockerType type,
            LockerDomainModel locker,
            UserDomainModel user
    ) {
        this.id = id;
        this.type = type;
        this.locker = locker;
        this.user = user;
    }

    public static LockerLogDomainModel of(LockerLog log) {
        return new LockerLogDomainModel(
                log.getId(),
                log.getLockerType(),
                LockerDomainModel.of(log.getLocker()),
                UserDomainModel.of(log.getUser())
        );
    }
}
