package net.causw.application.dto;

import lombok.Getter;
import net.causw.domain.model.LockerLogDomainModel;
import net.causw.infra.LockerType;

@Getter
public class LockerLogDetailDto {
    String id;
    Long lockerNumber;
    String userEmail;
    LockerType type;

    private LockerLogDetailDto(
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

    public static LockerLogDetailDto of(LockerLogDomainModel model) {
        return new LockerLogDetailDto(
                model.getId(),
                model.getLockerNumber(),
                model.getUserEmail(),
                model.getType()
        );
    }
}
