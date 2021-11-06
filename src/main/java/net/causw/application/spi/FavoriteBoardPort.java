package net.causw.application.spi;

import net.causw.domain.model.FavoriteBoardDomainModel;

import java.util.List;

public interface FavoriteBoardPort {
    FavoriteBoardDomainModel create(FavoriteBoardDomainModel favoriteBoardDomainModel);

    List<FavoriteBoardDomainModel> findByUserId(String userId);
}
