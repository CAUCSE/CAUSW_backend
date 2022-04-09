package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Locker;
import net.causw.adapter.persistence.LockerLocation;
import net.causw.adapter.persistence.LockerRepository;
import net.causw.adapter.persistence.User;
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
    public Optional<LockerDomainModel> findByUserId(String userId) {
        return this.lockerRepository.findByUser_Id(userId).map(this::entityToDomainModel);
    }

    @Override
    public LockerDomainModel create(LockerDomainModel lockerDomainModel) {
        return this.entityToDomainModel(this.lockerRepository.save(Locker.from(lockerDomainModel)));
    }

    @Override
    public Optional<LockerDomainModel> update(String id, LockerDomainModel lockerDomainModel) {
        return this.lockerRepository.findById(id).map(
                locker -> {
                    locker.setIsActive(lockerDomainModel.getIsActive());
                    locker.setUser(lockerDomainModel.getUser().map(User::from).orElse(null));

                    return this.entityToDomainModel(this.lockerRepository.save(locker));
                }
        );
    }

    @Override
    public Optional<LockerDomainModel> updateLocation(String id, LockerDomainModel lockerDomainModel) {
        return this.lockerRepository.findById(id).map(
                locker -> {
                    locker.setLocation(LockerLocation.from(lockerDomainModel.getLockerLocation()));

                    return this.entityToDomainModel(this.lockerRepository.save(locker));
                }
        );
    }

    @Override
    public void delete(LockerDomainModel lockerDomainModel) {
        this.lockerRepository.delete(Locker.from(lockerDomainModel));
    }

    @Override
    public List<LockerDomainModel> findByLocationId(String locationId) {
        return this.lockerRepository.findByLocation_IdOrderByLockerNumberAsc(locationId)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Long countEnableLockerByLocation(String locationId) {
        return this.lockerRepository.countEnableLockerByLocation(locationId);
    }

    @Override
    public Long countByLocation(String locationId) {
        return this.lockerRepository.countByLocation(locationId);
    }
}
