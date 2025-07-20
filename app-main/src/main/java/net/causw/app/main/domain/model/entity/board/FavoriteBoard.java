package net.causw.app.main.domain.model.entity.board;

import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
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

    public static FavoriteBoard of(
            User user,
            Board board
    ) {
        return FavoriteBoard.builder()
                .user(user)
                .board(board)
                .build();
    }
}
