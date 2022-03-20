package net.causw.application.spi;

import net.causw.domain.model.FavoriteBoardDomainModel;

import java.util.List;
import java.util.Optional;

public interface FavoriteBoardPort {
    FavoriteBoardDomainModel create(FavoriteBoardDomainModel favoriteBoardDomainModel);

    void delete(FavoriteBoardDomainModel favoriteBoardDomainModel);

    Optional<FavoriteBoardDomainModel> findByUserIdAndBoardId(String userId, String boardId);

    List<FavoriteBoardDomainModel> findByUserId(String userId);
}
