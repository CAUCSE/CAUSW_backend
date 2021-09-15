package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Locker;
import net.causw.adapter.persistence.LockerRepository;
import net.causw.application.spi.LockerPort;
import net.causw.domain.model.LockerDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LockerPortImpl implements LockerPort {
    private final LockerRepository lockerRepository;

    public LockerPortImpl(LockerRepository lockerRepository) {
        this.lockerRepository = lockerRepository;
    }

    @Override
    public Optional<LockerDomainModel> findById(String id) {
        return this.lockerRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public List<LockerDomainModel> findAll() {
        return this.lockerRepository.findAll()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    private LockerDomainModel entityToDomainModel(Locker locker) {
        return LockerDomainModel.of(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUpdatedAt(),
                locker.getUser().getId(),
                locker.getUser().getName()
        );
    }
}
