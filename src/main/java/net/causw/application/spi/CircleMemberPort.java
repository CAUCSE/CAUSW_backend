package net.causw.application.spi;

import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.CircleMemberDto;
import net.causw.application.dto.UserFullDto;
import net.causw.domain.model.CircleMemberStatus;

import java.util.Optional;

public interface CircleMemberPort {
    Optional<CircleMemberDto> findById(String id);

    Optional<CircleMemberDto> findByUserIdAndCircleId(String userId, String circleId);

    CircleMemberDto create(UserFullDto userFullDto, CircleFullDto circleFullDto);

    Optional<CircleMemberDto> updateStatus(String applicationId, CircleMemberStatus targetStatus);
}
