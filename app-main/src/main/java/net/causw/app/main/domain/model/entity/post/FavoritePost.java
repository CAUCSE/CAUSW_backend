package net.causw.app.main.domain.model.entity.post;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
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

    public static  FavoritePost of(Post post, User user, Boolean isDeleted) {
        return FavoritePost.builder()
                .post(post)
                .user(user)
                .isDeleted(isDeleted)
                .build();
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}


