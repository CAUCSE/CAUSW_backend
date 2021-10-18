package net.causw.application;

import net.causw.application.dto.LockerLocationResponseDto;
import net.causw.application.dto.LockerLogDetailDto;
import net.causw.application.dto.LockerResponseDto;
import net.causw.application.spi.LockerLocationPort;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerDomainModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LockerService {
    private final LockerPort lockerPort;
    private final LockerLocationPort lockerLocationPort;
    private final LockerLogPort lockerLogPort;

    public LockerService(LockerPort lockerPort, LockerLocationPort lockerLocationPort,LockerLogPort lockerLogPort) {
        this.lockerPort = lockerPort;
        this.lockerLocationPort = lockerLocationPort;
        this.lockerLogPort = lockerLogPort;
    }

    @Transactional(readOnly = true)
    public LockerResponseDto findById(String id) {
        return LockerResponseDto.from(this.lockerPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        ));
    }

    @Transactional(readOnly = true)
    public List<LockerResponseDto> findAll() {
        return this.lockerPort.findAll()
                .stream()
                .map(LockerResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LockerLocationResponseDto> findAllLocation() {
        return this.lockerLocationPort.findAll()
                .stream()
                .map(LockerLocationResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<LockerLogDetailDto> findLog(String id) {
        LockerDomainModel locker = this.lockerPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        );

        return this.lockerLogPort.findByLockerNumber(locker.getLockerNumber());
    }
}
