package net.causw.domain.spi;

import net.causw.domain.model.BoardDomainModel;

public interface BoardPort {
    BoardDomainModel findById(String id);
}
