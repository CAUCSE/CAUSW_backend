package net.causw.application.spi;

import net.causw.application.dto.LockerLogDetailDto;

import java.util.List;

public interface LockerLogPort {
    List<LockerLogDetailDto> findByLockerNumber(Long lockerNumber);
}
