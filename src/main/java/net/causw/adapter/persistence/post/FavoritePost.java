package net.causw.adapter.persistence.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_favorite_post")
public class FavoritePost extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    private FavoritePost(String id, Post post, User user, Boolean isDeleted) {
        super(id);
        this.post = post;
        this.user = user;
        this.isDeleted = isDeleted;
    }

    public static  FavoritePost of(Post post, User user, Boolean isDeleted) {
        return new FavoritePost(post, user, isDeleted);
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}


