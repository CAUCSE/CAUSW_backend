package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.CircleRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.CirclePort;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CirclePortImpl implements CirclePort {
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

    private CircleDomainModel entityToDomainModel(Circle circle) {
        return CircleDomainModel.of(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                this.entityToDomainModel(circle.getLeader())
        );
    }

    private UserDomainModel entityToDomainModel(User user) {
        return UserDomainModel.of(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState()
        );
    }
}
