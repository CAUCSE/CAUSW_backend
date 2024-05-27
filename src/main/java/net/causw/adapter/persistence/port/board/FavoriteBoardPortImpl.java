package net.causw.adapter.persistence.port.board;

import net.causw.adapter.persistence.board.FavoriteBoard;
import net.causw.adapter.persistence.port.mapper.DomainModelMapper;
import net.causw.adapter.persistence.repository.FavoriteBoardRepository;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.domain.model.board.FavoriteBoardDomainModel;
import org.springframework.stereotype.Component;

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
}
