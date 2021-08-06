package net.causw.application.spi;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface UserPort {
    Optional<UserFullDto> findById(String id);

    Optional<UserFullDto> findByName(String name);

    Optional<UserFullDto> findByEmail(String email);

    UserFullDto create(UserCreateRequestDto userCreateRequestDto);

    Optional<UserFullDto> update(String id, UserUpdateRequestDto userUpdateRequestDto);

    Optional<UserFullDto> updateRole(String id, Role role);

    List<UserFullDto> findByRole(Role role);
}
