package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.Board;
import net.causw.adapter.persistence.Circle;
import net.causw.adapter.persistence.FavoriteBoard;
import net.causw.adapter.persistence.FavoriteBoardRepository;
import net.causw.adapter.persistence.User;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.FavoriteBoardDomainModel;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FavoriteBoardPortImpl extends DomainModelMapper implements FavoriteBoardPort {
    private final FavoriteBoardRepository favoriteBoardRepository;

    public FavoriteBoardPortImpl(FavoriteBoardRepository favoriteBoardRepository) {
        this.favoriteBoardRepository = favoriteBoardRepository;
    }

    @Override
    public FavoriteBoardDomainModel create(FavoriteBoardDomainModel favoriteBoardDomainModel) {
        return this.entityToDomainModel(
                this.favoriteBoardRepository.save(FavoriteBoard.from(favoriteBoardDomainModel))
        );
    }

    @Override
    public List<FavoriteBoardDomainModel> findByUserId(String userId) {
        return this.favoriteBoardRepository.findByUser_Id(userId)
                .stream()
                .filter(favoriteBoard -> !favoriteBoard.getBoard().getIsDeleted())
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<FavoriteBoardDomainModel> findByBoardId(String boardId) {
        return this.favoriteBoardRepository.findByBoard_Id(boardId)
                .stream()
                .filter(favoriteBoard -> !favoriteBoard.getBoard().getIsDeleted())
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }
}
