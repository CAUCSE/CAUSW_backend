package net.causw.app.main.domain.model.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.user.User;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_comment_subscribe")
public class UserCommentSubscribe extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "is_subscribed")
    private Boolean isSubscribed;

    public void setIsSubscribed(Boolean subscribed) {
        this.isSubscribed = subscribed;
    }

    public static UserCommentSubscribe of(
            User user,
            Comment comment,
            Boolean isSubscribed
    ) {
        return UserCommentSubscribe.builder()
                .user(user)
                .comment(comment)
                .isSubscribed(isSubscribed)
                .build();
    }
}
