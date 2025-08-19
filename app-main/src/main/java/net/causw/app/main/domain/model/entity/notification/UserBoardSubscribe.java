package net.causw.app.main.domain.model.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.user.User;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_board_subscribe")
public class UserBoardSubscribe extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;


    @Column(name = "is_subscribed")
    private Boolean isSubscribed;


    public void setIsSubscribed(Boolean subscribed) {
        this.isSubscribed = subscribed;
    }



    public static UserBoardSubscribe of(
            User user,
            Board board,
            Boolean isSubscribed
    ) {
        return UserBoardSubscribe.builder()
                .user(user)
                .board(board)
                .isSubscribed(isSubscribed)
                .build();
    }

}
