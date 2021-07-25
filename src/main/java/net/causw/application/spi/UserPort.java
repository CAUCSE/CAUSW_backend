package net.causw.application.spi;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserUpdateRequestDto;

import java.util.Optional;

public interface UserPort {
    Optional<UserDetailDto> findById(String id);

    Optional<UserDetailDto> findByName(String name);

    Optional<UserFullDto> findByEmail(String email);

    UserDetailDto create(UserCreateRequestDto userCreateRequestDto);

    Optional<UserDetailDto> update(String id, UserUpdateRequestDto userUpdateRequestDto);
}
