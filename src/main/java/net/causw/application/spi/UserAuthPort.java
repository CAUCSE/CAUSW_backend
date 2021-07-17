package net.causw.application.spi;

import net.causw.application.dto.UserAuthDto;

public interface UserAuthPort {
    UserAuthDto findById(String id);
}
