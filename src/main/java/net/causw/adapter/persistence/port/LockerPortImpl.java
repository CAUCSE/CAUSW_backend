package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Locker;
import net.causw.adapter.persistence.LockerRepository;
import net.causw.application.spi.LockerPort;
import net.causw.domain.model.LockerDomainModel;
import net.causw.domain.model.LockerLocationDomainModel;
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


    private LockerDomainModel entityToDomainModel(Locker locker) {
        LockerLocationDomainModel lockerLocation = LockerLocationDomainModel.of(
                locker.getLocation().getId(),
                locker.getLocation().getName(),
                locker.getLocation().getDescription()
        );
        return LockerDomainModel.of(
                locker.getId(),
                locker.getLockerNumber(),
                locker.getIsActive(),
                locker.getUpdatedAt(),
                locker.getUser().getId(),
                locker.getUser().getName(),
                lockerLocation
        );
    }
}
