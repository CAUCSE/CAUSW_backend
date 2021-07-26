package net.causw.application;

import net.causw.application.dto.LockerDetailDto;
import net.causw.application.dto.LockerLogDetailDto;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LockerService {
    private final LockerPort lockerPort;
    private final LockerLogPort lockerLogPort;

    public LockerService(LockerPort lockerPort, LockerLogPort lockerLogPort) {
        this.lockerPort = lockerPort;
        this.lockerLogPort = lockerLogPort;
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

    public List<LockerLogDetailDto> findLog(String id) {
        LockerDetailDto locker = this.lockerPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        );

        return this.lockerLogPort.findByLockerNumber(locker.getLockerNumber());
    }
}
