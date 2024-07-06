package net.causw.adapter.persistence.board;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.board.FavoriteBoardDomainModel;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "TB_FAVORITE_BOARD")
public class FavoriteBoard extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    private FavoriteBoard(
            String id,
            User user,
            Board board
    ) {
        super(id);
        this.user = user;
        this.board = board;
    }

    public static FavoriteBoard from(FavoriteBoardDomainModel favoriteBoardDomainModel) {
        return new FavoriteBoard(
                favoriteBoardDomainModel.getId(),
                User.from(favoriteBoardDomainModel.getUserDomainModel()),
                Board.from(favoriteBoardDomainModel.getBoardDomainModel())
        );
    }

    public static FavoriteBoard of(
            User user,
            Board board
    ) {
        return new FavoriteBoard(user, board);
    }
}
