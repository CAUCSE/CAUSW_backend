package net.causw.adapter.persistence.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.comment.Comment;
import net.causw.adapter.persistence.user.User;

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

    @Setter
    @Column(name = "is_subscribed")
    private Boolean isSubscribed;

    public UserCommentSubscribe toggle() {
        this.setIsSubscribed(!this.getIsSubscribed());
        return this;
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
