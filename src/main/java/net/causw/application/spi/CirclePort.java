package net.causw.application.spi;

import net.causw.application.dto.CircleDto;
import net.causw.application.dto.UserFullDto;

import java.util.Optional;

public interface CirclePort {
    Optional<CircleDto> findById(String id);
    Optional<CircleDto> findByLeaderId(String leaderId);
    Optional<CircleDto> updateLeader(String id, UserFullDto newLeader);
}
