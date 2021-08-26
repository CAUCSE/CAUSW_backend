package net.causw.application.spi;

import net.causw.application.dto.CircleCreateRequestDto;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.UserFullDto;

import java.util.Optional;

public interface CirclePort {
    Optional<CircleFullDto> findById(String id);

    Optional<CircleFullDto> findByLeaderId(String leaderId);

    Optional<CircleFullDto> findByName(String name);

    CircleFullDto create(CircleCreateRequestDto circleCreateRequestDto, UserFullDto leader);

    Optional<CircleFullDto> updateLeader(String id, UserFullDto newLeader);
}
