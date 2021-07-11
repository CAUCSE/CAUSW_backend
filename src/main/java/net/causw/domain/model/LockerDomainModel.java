package net.causw.domain.model;

import lombok.Getter;
import net.causw.infra.Locker;
import net.causw.infra.User;

@Getter
public class LockerDomainModel {
    private String id;
    private Long lockerNumber;
    private User user;

    private LockerDomainModel(
            String id,
            Long lockerNumber,
            User user
    ) {
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.user = user;
    }

    public static LockerDomainModel of(Locker locker) {
        return new LockerDomainModel(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getUser()
        );
    }
}
