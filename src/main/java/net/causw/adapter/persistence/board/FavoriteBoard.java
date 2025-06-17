package net.causw.adapter.persistence.board;

import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_favorite_board")
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
