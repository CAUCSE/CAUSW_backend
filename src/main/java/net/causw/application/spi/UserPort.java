package net.causw.application.spi;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;

public interface UserPort {
    UserDetailDto findById(String id);
    UserDetailDto create(UserCreateRequestDto user);
}
