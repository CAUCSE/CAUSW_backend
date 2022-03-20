package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.FavoriteBoard;
import net.causw.adapter.persistence.FavoriteBoardRepository;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.domain.model.FavoriteBoardDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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
    public void delete(FavoriteBoardDomainModel favoriteBoardDomainModel) {
        this.favoriteBoardRepository.delete(FavoriteBoard.from(favoriteBoardDomainModel));
    }

    @Override
    public Optional<FavoriteBoardDomainModel> findByUserIdAndBoardId(String userId, String boardId) {
        return this.favoriteBoardRepository.findByUser_IdAndBoard_Id(userId, boardId).map(this::entityToDomainModel);
    }

    @Override
    public List<FavoriteBoardDomainModel> findByUserId(String userId) {
        return this.favoriteBoardRepository.findByUser_Id(userId)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }
}
