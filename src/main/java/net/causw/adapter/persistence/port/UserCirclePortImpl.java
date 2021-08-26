package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.User;
import net.causw.adapter.persistence.UserCircle;
import net.causw.adapter.persistence.UserCircleRepository;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.UserCircleDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.UserCirclePort;
import net.causw.domain.model.UserCircleStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserCirclePortImpl implements UserCirclePort {
    private final UserCircleRepository userCircleRepository;

    public UserCirclePortImpl(UserCircleRepository userCircleRepository) {
        this.userCircleRepository = userCircleRepository;
    }

    @Override
    public Optional<UserCircleDto> findById(String id) {
        return this.userCircleRepository.findById(id).map(UserCircleDto::from);
    }

    @Override
    public Optional<UserCircleStatus> loadUserCircleStatus(String userId, String circleId) {
        return this.userCircleRepository.findByUser_IdAndCircle_Id(userId, circleId).map(UserCircle::getStatus);
    }

    @Override
    public UserCircleDto create(UserFullDto userFullDto, CircleFullDto circleFullDto) {
        return UserCircleDto.from(this.userCircleRepository.save(UserCircle.of(
                UserCircleStatus.AWAIT,
                Circle.from(circleFullDto),
                User.from(userFullDto)
        )));
    }

    @Override
    public Optional<UserCircleDto> accept(String userId, String circleId) {
        return this.setStatus(
                userId,
                circleId,
                UserCircleStatus.MEMBER
        );
    }

    private Optional<UserCircleDto> setStatus(
            String userId,
            String circleId,
            UserCircleStatus status
    ) {
        return this.userCircleRepository.findByUser_IdAndCircle_Id(userId, circleId).map(
                userCircle -> {
                    userCircle.setStatus(status);

                    return UserCircleDto.from(this.userCircleRepository.save(userCircle));
                }
        );
    }
}
