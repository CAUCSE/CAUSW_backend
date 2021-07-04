package net.causw.domain.spi;

import net.causw.domain.model.UserDomainModel;

public interface UserPort {
    UserDomainModel findById(String id);
}
