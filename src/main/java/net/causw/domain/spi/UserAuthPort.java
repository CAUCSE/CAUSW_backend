package net.causw.domain.spi;

import net.causw.infra.UserAuth;

public interface UserAuthPort {
    UserAuth findById(String id);
}
