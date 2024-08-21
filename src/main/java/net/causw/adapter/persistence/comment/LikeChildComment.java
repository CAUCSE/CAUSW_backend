package net.causw.adapter.persistence.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;

@Getter
@Entity
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

    private LikeChildComment(String id, ChildComment childComment, User user) {
        super(id);
        this.childComment = childComment;
        this.user = user;
    }

    public static LikeChildComment of(ChildComment childComment, User user) {
        return new LikeChildComment(childComment, user);
    }

}
