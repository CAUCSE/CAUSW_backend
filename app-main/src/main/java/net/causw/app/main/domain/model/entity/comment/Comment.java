package net.causw.app.main.domain.model.entity.comment;

import lombok.*;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_comment")
public class Comment extends BaseEntity {
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "is_anonymous", nullable = false)
    @ColumnDefault("false")
    private Boolean isAnonymous;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @ManyToOne(targetEntity = Post.class)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Setter
	@OneToMany(mappedBy = "parentComment")
    @Builder.Default
    private List<ChildComment> childCommentList = new ArrayList<>(); // 필드 초기화 없으면 NPE

    public static Comment of(
            String content,
            Boolean isDeleted,
            Boolean isAnonymous,
            User writer,
            Post post
    ) {
        return Comment.builder()
                .content(content)
                .isDeleted(isDeleted)
                .isAnonymous(isAnonymous)
                .writer(writer)
                .post(post)
                .build();
    }

	public void update(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
