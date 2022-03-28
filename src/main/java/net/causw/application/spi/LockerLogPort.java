package net.causw.application.spi;

import net.causw.application.dto.locker.LockerLogResponseDto;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.UserDomainModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LockerLogPort {
    List<LockerLogResponseDto> findByLockerNumber(Long lockerNumber);

    void create(Long lockerNumber, String lockerLocationName, UserDomainModel user, LockerLogAction action, String message);

    Optional<LocalDateTime> whenRegister(UserDomainModel user);
}
