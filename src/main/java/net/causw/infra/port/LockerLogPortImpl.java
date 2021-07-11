package net.causw.infra.port;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerLogDomainModel;
import net.causw.domain.spi.LockerLogPort;
import net.causw.infra.LockerLogRepository;
import org.springframework.stereotype.Component;

@Component
public class LockerLogPortImpl implements LockerLogPort {
    private final LockerLogRepository lockerLogRepository;

    public LockerLogPortImpl(LockerLogRepository lockerLogRepository) {
        this.lockerLogRepository = lockerLogRepository;
    }

    @Override
    public LockerLogDomainModel findById(String id) {
        return LockerLogDomainModel.of(lockerLogRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker log id"
                )
        ));
    }
}
