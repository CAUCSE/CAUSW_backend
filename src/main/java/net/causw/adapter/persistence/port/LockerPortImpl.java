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
public class LockerPortImpl extends DomainModelMapper implements LockerPort {
    private final LockerRepository lockerRepository;

    public LockerPortImpl(LockerRepository lockerRepository) {
        this.lockerRepository = lockerRepository;
    }

    @Override
    public Optional<LockerDomainModel> findById(String id) {
        return this.lockerRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public Optional<LockerDomainModel> findByLockerNumber(Long lockerNumber) {
        return this.lockerRepository.findByLockerNumber(lockerNumber).map(this::entityToDomainModel);
    }

    @Override
    public LockerDomainModel create(LockerDomainModel lockerDomainModel) {
        return this.entityToDomainModel(this.lockerRepository.save(Locker.from(lockerDomainModel)));
    }

    @Override
    public List<LockerDomainModel> findByLocationId(String locationId) {
        return this.lockerRepository.findByLocation_Id(locationId)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Long getEnableLockerCountByLocation(String locationId) {
        return this.lockerRepository.getEnableLockerCountByLocation(locationId);
    }

    @Override
    public Long getLockerCountByLocation(String locationId) {
        return this.lockerRepository.getLockerCountByLocation(locationId);
    }
}
