package net.causw.application.dto;

import lombok.Getter;
import net.causw.domain.model.LockerDomainModel;

@Getter
public class LockerDetailDto {
    private String id;
    private Long lockerNumber;
    private UserDetailDto user;

    private LockerDetailDto(
            String id,
            Long lockerNumber,
            UserDetailDto user
    ){
        this.id = id;
        this.lockerNumber = lockerNumber;
        this.user = user;
    }

    public static LockerDetailDto of(LockerDomainModel locker) {
        return new LockerDetailDto(
                locker.getId(),
                locker.getLockerNumber(),
                UserDetailDto.of(locker.getUser())
        );
    }
}
