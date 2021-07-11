package net.causw.domain.model;

import lombok.Getter;
import net.causw.infra.Locker;

@Getter
public class LockerDomainModel {
    private String id;
    private Long lockerNumber;
    private UserDomainModel user;

    private LockerDomainModel(
            String id,
            Long lockerNumber,
            UserDomainModel user
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.user = user;
    }

    public static LockerDomainModel of(Locker locker) {
        return new LockerDomainModel(
                locker.getId(),
                locker.getLockerNumber(),
                UserDomainModel.of(locker.getUser())
        );
    }
}
