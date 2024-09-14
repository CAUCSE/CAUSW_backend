package net.causw.adapter.persistence.comment;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_like_child_comment")
public class LikeChildComment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_comment_id")
    private ChildComment childComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static LikeChildComment of(ChildComment childComment, User user) {
        return LikeChildComment.builder()
                .childComment(childComment)
                .user(user)
                .build();
    }

}
