package net.causw.adapter.persistence.port.locker;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.locker.LockerLocation;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.repository.LockerRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.spi.LockerPort;
import net.causw.domain.model.locker.LockerDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LockerPortImpl extends DomainModelMapper implements LockerPort {
    private final LockerRepository lockerRepository;

    @Override
    public Optional<LockerDomainModel> findByIdForRead(String id) {
        return this.lockerRepository.findByIdForRead(id).map(this::entityToDomainModel);
    }

    @Override
    public Optional<LockerDomainModel> findByIdForWrite(String id) {
        return this.lockerRepository.findByIdForWrite(id).map(this::entityToDomainModel);
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
                    locker.setExpireDate(lockerDomainModel.getExpiredAt());

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
        return this.lockerRepository.countByLocationIdAndIsActiveIsTrueAndUserIdIsNull(locationId);
    }

    @Override
    public Long countByLocation(String locationId) {
        return this.lockerRepository.countByLocationId(locationId);
    }
}
