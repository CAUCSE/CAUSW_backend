package net.causw.domain.spi;

import net.causw.domain.model.UserDomainModel;
import net.causw.infra.User;

public interface UserPort {
    UserDomainModel findById(String id);

    UserDomainModel save(User user);
}
