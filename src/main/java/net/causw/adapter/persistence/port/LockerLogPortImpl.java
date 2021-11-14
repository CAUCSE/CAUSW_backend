package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.LockerLog;
import net.causw.adapter.persistence.LockerLogRepository;
import net.causw.application.dto.LockerLogDetailDto;
import net.causw.application.spi.LockerLogPort;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LockerLogPortImpl extends DomainModelMapper implements LockerLogPort {
    private final LockerLogRepository lockerLogRepository;

    public LockerLogPortImpl(LockerLogRepository lockerLogRepository) {
        this.lockerLogRepository = lockerLogRepository;
    }

    @Override
    public List<LockerLogDetailDto> findByLockerNumber(Long lockerNumber) {
        return this.lockerLogRepository.findByLockerNumber(lockerNumber)
                .stream()
                .map(LockerLogDetailDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public void create(
            Long lockerNumber,
            UserDomainModel user,
            LockerLogAction action,
            String message
    ) {
        this.lockerLogRepository.save(LockerLog.of(
                lockerNumber,
                user.getEmail(),
                user.getName(),
                action,
                message
        ));
    }
}
