package net.causw.application;

import net.causw.application.dto.LockerDetailDto;
import net.causw.application.spi.LockerPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LockerService {
    private final LockerPort lockerPort;

    public LockerService(LockerPort lockerPort) {
        this.lockerPort = lockerPort;
    }

    @Transactional(readOnly = true)
    public LockerDetailDto findById(String id) {
        return this.lockerPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        );
    }

    @Transactional(readOnly = true)
    public List<LockerDetailDto> findAll() {
        return this.lockerPort.findAll();
    }
}
