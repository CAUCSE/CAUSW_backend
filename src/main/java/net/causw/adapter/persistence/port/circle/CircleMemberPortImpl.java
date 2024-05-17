package net.causw.adapter.persistence.port.circle;

import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.repository.CircleMemberRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.spi.CircleMemberPort;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.user.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CircleMemberPortImpl extends DomainModelMapper implements CircleMemberPort {
    private final CircleMemberRepository circleMemberRepository;

    public CircleMemberPortImpl(CircleMemberRepository circleMemberRepository) {
        this.circleMemberRepository = circleMemberRepository;
    }

    @Override
    public Optional<CircleMemberDomainModel> findById(String id) {
        return this.circleMemberRepository.findById(id).map(this::entityToDomainModel);
    }

    @Override
    public List<CircleMemberDomainModel> findByUserId(String userId) {
        return this.circleMemberRepository.findByUser_Id(userId)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, CircleMemberDomainModel> findCircleByUserId(String userId) {
        return this.circleMemberRepository.findByUser_Id(userId)
                .stream()
                .map(this::entityToDomainModel)
                .filter(circleMemberDomainModel -> circleMemberDomainModel.getStatus().equals(CircleMemberStatus.MEMBER))
                .collect(Collectors.toMap(
                        circleMemberDomainModel -> circleMemberDomainModel.getCircle().getId(),
                        circleMemberDomainModel -> circleMemberDomainModel
                ));
    }

    @Override
    public List<CircleDomainModel> getCircleListByUserId(String userId) {
        return this.circleMemberRepository.findByUser_Id(userId)
                .stream()
                .filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER)
                .map(circleMember -> this.entityToDomainModel(circleMember.getCircle()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CircleMemberDomainModel> findByCircleId(String circleId, CircleMemberStatus status) {
        return this.circleMemberRepository.findByCircle_Id(circleId)
                .stream()
                .filter(circleMember -> circleMember.getStatus() == status)
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CircleMemberDomainModel> findByUserIdAndCircleId(String userId, String circleId) {
        return this.circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).map(this::entityToDomainModel);
    }

    @Override
    public Long getNumMember(String id) {
        return this.circleMemberRepository.getNumMember(id);
    }

    @Override
    public CircleMemberDomainModel create(UserDomainModel userDomainModel, CircleDomainModel circleDomainModel) {
        return this.entityToDomainModel(this.circleMemberRepository.save(CircleMember.of(
                CircleMemberStatus.AWAIT,
                Circle.from(circleDomainModel),
                User.from(userDomainModel)
        )));
    }

    @Override
    public Optional<CircleMemberDomainModel> updateStatus(String applicationId, CircleMemberStatus targetStatus) {
        return this.circleMemberRepository.findById(applicationId).map(
                circleMember -> {
                    circleMember.setStatus(targetStatus);

                    return this.entityToDomainModel(this.circleMemberRepository.save(circleMember));
                }
        );
    }
}
