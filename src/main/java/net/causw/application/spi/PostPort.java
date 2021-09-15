package net.causw.application.spi;

import net.causw.domain.model.PostDomainModel;

public interface PostPort {
    PostDomainModel findById(String id);
}
