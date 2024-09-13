package net.causw.adapter.persistence.comment;

import lombok.*;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
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

    @OneToMany(mappedBy = "parentComment")
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
                .childCommentList(new ArrayList<>())
                .build();
    }

    public void setChildCommentList(List<ChildComment> childCommentList) {
        this.childCommentList = childCommentList;
    }

    public void update(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
