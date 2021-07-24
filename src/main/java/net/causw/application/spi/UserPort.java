package net.causw.application.spi;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;

import java.util.Optional;

public interface UserPort {
    UserDetailDto findById(String id);

    Optional<UserFullDto> findByEmail(String email);

    UserDetailDto create(UserCreateRequestDto user);
}
