package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.LockerLocation;
import net.causw.adapter.persistence.LockerLocationRepository;
import net.causw.application.spi.LockerLocationPort;
import net.causw.domain.model.LockerLocationDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LockerLocationPortImpl implements LockerLocationPort {
    private final LockerLocationRepository lockerLocationRepository;

    public LockerLocationPortImpl(LockerLocationRepository lockerLocationRepository) {
        this.lockerLocationRepository = lockerLocationRepository;
    }

    @Override
    public Optional<LockerLocationDomainModel> findById(String id) {
        return this.lockerLocationRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public List<LockerLocationDomainModel> findAll() {
        return this.lockerLocationRepository.findAll()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    private LockerLocationDomainModel entityToDomainModel(LockerLocation lockerLocation) {
        return LockerLocationDomainModel.of(
                lockerLocation.getId(),
                lockerLocation.getLocation(),
                lockerLocation.getLocationDesc()
        );
    }
}
