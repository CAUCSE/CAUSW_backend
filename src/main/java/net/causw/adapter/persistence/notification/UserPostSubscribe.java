package net.causw.adapter.persistence.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_post_subscribe")
public class UserPostSubscribe extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "is_subscribed")
    private Boolean isSubscribed;

    public void setIsSubscribed(Boolean subscribed) {
        this.isSubscribed = subscribed;
    }

    public static UserPostSubscribe of(
            User user,
            Post post,
            Boolean isSubscribed
    ) {
        return UserPostSubscribe.builder()
                .user(user)
                .post(post)
                .isSubscribed(isSubscribed)
                .build();
    }
}
