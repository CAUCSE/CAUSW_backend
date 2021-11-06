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
public class FavoriteBoardPortImpl implements FavoriteBoardPort {
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

    private FavoriteBoardDomainModel entityToDomainModel(FavoriteBoard favoriteBoard) {
        return FavoriteBoardDomainModel.of(
                favoriteBoard.getId(),
                this.entityToDomainModel(favoriteBoard.getUser()),
                this.entityToDomainModel(favoriteBoard.getBoard())
        );
    }

    private UserDomainModel entityToDomainModel(User user) {
        return UserDomainModel.of(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState()
        );
    }

    private BoardDomainModel entityToDomainModel(Board board) {
        CircleDomainModel circleDomainModel = null;
        if (board.getCircle() != null) {
            circleDomainModel = this.entityToDomainModel(board.getCircle());
        }

        return BoardDomainModel.of(
                board.getId(),
                board.getName(),
                board.getDescription(),
                new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))),
                board.getCategory(),
                board.getIsDeleted(),
                circleDomainModel
        );
    }

    private CircleDomainModel entityToDomainModel(Circle circle) {
        return CircleDomainModel.of(
                circle.getId(),
                circle.getName(),
                circle.getMainImage(),
                circle.getDescription(),
                circle.getIsDeleted(),
                this.entityToDomainModel(circle.getLeader()),
                circle.getCreatedAt(),
                circle.getUpdatedAt()
        );
    }
}
