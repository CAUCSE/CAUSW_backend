package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.CommentDomainModel;
import net.causw.domain.model.PostDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_COMMENT")
public class Comment extends BaseEntity {
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @ManyToOne(targetEntity = Post.class)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @OneToMany(mappedBy = "parentComment")
    private List<ChildComment> childCommentList;

    private Comment(
            String content,
            Boolean isDeleted,
            User writer,
            Post post
    ) {
        this.content = content;
        this.isDeleted = isDeleted;
        this.writer = writer;
        this.post = post;
    }

    private Comment(
            String id,
            String content,
            Boolean isDeleted,
            User writer,
            Post post
    ) {
        super(id);
        this.content = content;
        this.isDeleted = isDeleted;
        this.writer = writer;
        this.post = post;
    }

    public static Comment of(
            String content,
            Boolean isDeleted,
            User writer,
            Post post
    ) {
        return new Comment(
                content,
                isDeleted,
                writer,
                post
        );
    }

    public static Comment of(
            String id,
            String content,
            Boolean isDeleted,
            User writer,
            Post post
    ) {
        return new Comment(
                id,
                content,
                isDeleted,
                writer,
                post
        );
    }

    public static Comment from(CommentDomainModel commentDomainModel, PostDomainModel postDomainModel) {
        return new Comment(
                commentDomainModel.getId(),
                commentDomainModel.getContent(),
                commentDomainModel.getIsDeleted(),
                User.from(commentDomainModel.getWriter()),
                Post.from(postDomainModel)
        );
    }
}
