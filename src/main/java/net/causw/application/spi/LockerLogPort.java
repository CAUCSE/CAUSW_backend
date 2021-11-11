package net.causw.application.spi;

import net.causw.application.dto.LockerLogDetailDto;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.UserDomainModel;

import java.util.List;

public interface LockerLogPort {
    List<LockerLogDetailDto> findByLockerNumber(Long lockerNumber);

    void create(Long lockerNumber, UserDomainModel user, LockerLogAction action, String message);
}
