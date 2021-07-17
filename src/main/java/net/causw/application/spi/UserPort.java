package net.causw.application.spi;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;

public interface UserPort {
    UserDetailDto findById(String id);

    UserFullDto findByEmail(String email);

    UserDetailDto create(UserCreateRequestDto user);
}
