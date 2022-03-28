package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.BaseEntity;
import net.causw.adapter.persistence.LockerLog;
import net.causw.adapter.persistence.LockerLogRepository;
import net.causw.application.dto.locker.LockerLogResponseDto;
import net.causw.application.spi.LockerLogPort;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LockerLogPortImpl extends DomainModelMapper implements LockerLogPort {
    private final LockerLogRepository lockerLogRepository;

    public LockerLogPortImpl(LockerLogRepository lockerLogRepository) {
        this.lockerLogRepository = lockerLogRepository;
    }

    @Override
    public List<LockerLogResponseDto> findByLockerNumber(Long lockerNumber) {
        return this.lockerLogRepository.findByLockerNumber(lockerNumber)
                .stream()
                .map(LockerLogResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public void create(
            Long lockerNumber,
            String lockerLocationName,
            UserDomainModel user,
            LockerLogAction action,
            String message
    ) {
        this.lockerLogRepository.save(LockerLog.of(
                lockerNumber,
                lockerLocationName,
                user.getEmail(),
                user.getName(),
                action,
                message
        ));
    }

    @Override
    public Optional<LocalDateTime> whenRegister(UserDomainModel user) {
        return this.lockerLogRepository.findTopByUserEmailAndActionOrderByCreatedAtDesc(
                        user.getEmail(),
                        LockerLogAction.REGISTER)
                .map(BaseEntity::getCreatedAt);
    }
}
