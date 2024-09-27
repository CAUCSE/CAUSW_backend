package net.causw.adapter.persistence.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.user.User;

@Getter
@Setter
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

    public UserBoardSubscribe toggle() {
        this.setIsSubscribed(!this.getIsSubscribed());
        return this;
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
