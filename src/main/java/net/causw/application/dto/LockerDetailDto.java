package net.causw.application.dto;

import lombok.Getter;
import net.causw.domain.model.LockerDomainModel;
import net.causw.infra.User;

@Getter
public class LockerDetailDto {
    private String id;
    private Long lockerNumber;
    private User user;

    private LockerDetailDto(String id, Long lockerNumber, User user){
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.user = user;
    }

    public static LockerDetailDto of(LockerDomainModel locker) {
        return new LockerDetailDto(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getUser()
        );
    }
}
