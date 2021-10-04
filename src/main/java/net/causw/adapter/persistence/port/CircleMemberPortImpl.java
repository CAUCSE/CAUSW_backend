package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.CircleMember;
import net.causw.adapter.persistence.CircleMemberRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.CircleMemberPort;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CircleMemberPortImpl implements CircleMemberPort {
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

    private CircleMemberDomainModel entityToDomainModel(CircleMember circleMember) {
        return CircleMemberDomainModel.of(
                circleMember.getId(),
                circleMember.getStatus(),
                this.entityToDomainModel(circleMember.getCircle()),
                circleMember.getUser().getId(),
                circleMember.getUser().getName(),
                circleMember.getCreatedAt(),
                circleMember.getUpdatedAt()
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
