package net.causw.application;

import net.causw.application.dto.LockerLogDetailDto;
import net.causw.domain.spi.LockerLogPort;
import org.springframework.stereotype.Service;

@Service
public class LockerLogService {
    LockerLogPort lockerLogPort;

    public LockerLogService(LockerLogPort lockerLogPort) {
        this.lockerLogPort = lockerLogPort;
    }

    public LockerLogDetailDto findById(String id) {
        return LockerLogDetailDto.of(lockerLogPort.findById(id));
    }
}
