package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.CircleRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.CirclePort;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CirclePortImpl extends DomainModelMapper implements CirclePort {
    private final CircleRepository circleRepository;

    public CirclePortImpl(CircleRepository circleRepository) {
        this.circleRepository = circleRepository;
    }

    @Override
    public Optional<CircleDomainModel> findById(String id) {
        return this.circleRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public Optional<CircleDomainModel> findByLeaderId(String leaderId) {
        return this.circleRepository.findByLeaderId(leaderId).map(this::entityToDomainModel);

    }

    @Override
    public List<CircleDomainModel> findAll() {
        return this.circleRepository.findAllByIsDeletedIsFalse()
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CircleDomainModel> findByName(String name) {
        return this.circleRepository.findByName(name).map(this::entityToDomainModel);
    }

    @Override
    public CircleDomainModel create(CircleDomainModel circleDomainModel) {
        return this.entityToDomainModel(this.circleRepository.save(Circle.from(circleDomainModel)));
    }

    @Override
    public Optional<CircleDomainModel> update(String id, CircleDomainModel circleDomainModel) {
        return this.circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.setDescription(circleDomainModel.getDescription());
                    srcCircle.setName(circleDomainModel.getName());
                    srcCircle.setMainImage(circleDomainModel.getMainImage());

                    return this.entityToDomainModel(this.circleRepository.save(srcCircle));
                }
        );
    }

    @Override
    public Optional<CircleDomainModel> updateLeader(String id, UserDomainModel newLeader) {
        return this.circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.setLeader(User.from(newLeader));

                    return this.entityToDomainModel(this.circleRepository.save(srcCircle));
                }
        );
    }

    @Override
    public Optional<CircleDomainModel> delete(String id) {
        return this.circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.setIsDeleted(true);
                    srcCircle.setLeader(null);

                    return this.entityToDomainModel(this.circleRepository.save(srcCircle));
                }
        );
    }
}
