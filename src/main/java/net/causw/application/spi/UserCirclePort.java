package net.causw.application.spi;

import net.causw.application.dto.UserCircleDto;

import java.util.Optional;

public interface UserCirclePort {
    Optional<UserCircleDto> findById(String id);
}
