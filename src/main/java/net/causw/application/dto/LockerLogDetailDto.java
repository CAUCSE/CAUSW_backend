package net.causw.application.dto;

import lombok.Getter;
import net.causw.domain.model.LockerLogDomainModel;
import net.causw.infra.LockerType;

@Getter
public class LockerLogDetailDto {
    String id;
    LockerType type;
    LockerDetailDto locker;
    UserDetailDto user;

    private LockerLogDetailDto(
            String id,
            LockerType type,
            LockerDetailDto locker,
            UserDetailDto user
    ) {
        this.id = id;
        this.type = type;
        this.locker = locker;
        this.user = user;
    }

    public static LockerLogDetailDto of(LockerLogDomainModel model) {
        return new LockerLogDetailDto(
                model.getId(),
                model.getType(),
                LockerDetailDto.of(model.getLocker()),
                UserDetailDto.of(model.getUser())
        );
    }
}
