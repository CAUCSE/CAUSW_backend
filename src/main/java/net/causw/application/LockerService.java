package net.causw.application;

import net.causw.application.dto.LockerDetailDto;
import net.causw.domain.spi.LockerPort;
import org.springframework.stereotype.Service;

@Service
public class LockerService {
    private final LockerPort lockerPort;

    public LockerService(LockerPort lockerPort) {
        this.lockerPort = lockerPort;
    }

    public LockerDetailDto findById(String id) {
        return LockerDetailDto.of(lockerPort.findById(id));
    }

    public LockerDetailDto findByLockerNumber(Long lockerNumber) {
        return LockerDetailDto.of(lockerPort.findByLockerNumber(lockerNumber));
    }
}
