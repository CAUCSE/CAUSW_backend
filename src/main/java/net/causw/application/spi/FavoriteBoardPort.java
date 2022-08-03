package net.causw.application.spi;

import net.causw.domain.model.FavoriteBoardDomainModel;
import net.causw.domain.model.UserDomainModel;

import java.util.List;

public interface FavoriteBoardPort {
    FavoriteBoardDomainModel create(FavoriteBoardDomainModel favoriteBoardDomainModel);

    List<FavoriteBoardDomainModel> findByUserId(String userId);

    List<FavoriteBoardDomainModel> findByBoardId(String boardId);
}
