package net.causw.domain.spi;

import net.causw.domain.model.PostDomainModel;

public interface PostPort {
    PostDomainModel findById(String id);
}
