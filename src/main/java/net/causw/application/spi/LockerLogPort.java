package net.causw.application.spi;

import net.causw.application.dto.LockerLogDetailDto;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LockerLogPort {
    List<LockerLogDetailDto> findByLockerNumber(Long lockerNumber);

    void create(Long lockerNumber, UserDomainModel user, LockerLogAction action, String message);

    Optional<LocalDateTime> whenRegister(UserDomainModel user);
}
