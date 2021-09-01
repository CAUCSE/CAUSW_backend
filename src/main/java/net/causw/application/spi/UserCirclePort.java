package net.causw.application.spi;

import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.UserCircleDto;
import net.causw.application.dto.UserFullDto;
import net.causw.domain.model.UserCircleStatus;

import java.util.Optional;

public interface UserCirclePort {
    Optional<UserCircleDto> findById(String id);

    Optional<UserCircleStatus> loadUserCircleStatus(String userId, String circleId);

    UserCircleDto create(UserFullDto userFullDto, CircleFullDto circleFullDto);

    Optional<UserCircleDto> accept(String userId, String circleId);
}
