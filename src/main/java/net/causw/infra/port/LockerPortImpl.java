package net.causw.infra.port;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.spi.LockerPort;
import net.causw.infra.LockerRepository;
import org.springframework.stereotype.Component;

@Component
public class LockerPortImpl implements LockerPort {
    private final LockerRepository lockerRepository;

    public LockerPortImpl(LockerRepository lockerRepository) {
        this.lockerRepository = lockerRepository;
    }

    @Override
    public LockerDomainModel findById(String id) {
        return LockerDomainModel.of(lockerRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker id"
                )
        ));
    }

    @Override
    public LockerDomainModel findByLockerNumber(Long lockerNumber) {
        return LockerDomainModel.of(lockerRepository.findByLockerNumber(lockerNumber).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid locker number"
                )
        ));
    }
}
