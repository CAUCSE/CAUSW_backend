package net.causw.adapter.persistence.port.circle;

import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.repository.CircleRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.spi.CirclePort;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.user.UserDomainModel;
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
    public List<CircleDomainModel> findByLeaderId(String leaderId) {
        List<Circle> circles = this.circleRepository.findByLeader_Id(leaderId);
        return circles.stream().map(this::entityToDomainModel).collect(Collectors.toList());
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
                    srcCircle.update(circleDomainModel.getDescription(), circleDomainModel.getName(), circleDomainModel.getMainImage());

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
                    srcCircle.delete();

                    return this.entityToDomainModel(this.circleRepository.save(srcCircle));
                }
        );
    }
}
