package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.CircleMember;
import net.causw.adapter.persistence.CircleMemberRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.CircleMemberDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.CircleMemberPort;
import net.causw.domain.model.CircleMemberStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CircleMemberPortImpl implements CircleMemberPort {
    private final CircleMemberRepository circleMemberRepository;

    public CircleMemberPortImpl(CircleMemberRepository circleMemberRepository) {
        this.circleMemberRepository = circleMemberRepository;
    }

    @Override
    public Optional<CircleMemberDto> findById(String id) {
        return this.circleMemberRepository.findById(id).map(CircleMemberDto::from);
    }

    @Override
    public Optional<CircleMemberDto> findByUserIdAndCircleId(String userId, String circleId) {
        return this.circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).map(CircleMemberDto::from);
    }

    @Override
    public CircleMemberDto create(UserFullDto userFullDto, CircleFullDto circleFullDto) {
        return CircleMemberDto.from(this.circleMemberRepository.save(CircleMember.of(
                CircleMemberStatus.AWAIT,
                Circle.from(circleFullDto),
                User.from(userFullDto)
        )));
    }

    @Override
    public Optional<CircleMemberDto> updateStatus(String applicationId, CircleMemberStatus targetStatus) {
        return this.circleMemberRepository.findById(applicationId).map(
                circleMember -> {
                    circleMember.setStatus(targetStatus);

                    return CircleMemberDto.from(this.circleMemberRepository.save(circleMember));
                }
        );
    }
}
