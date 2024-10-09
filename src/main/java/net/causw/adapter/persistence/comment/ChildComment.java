package net.causw.adapter.persistence.comment;

import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_child_comment")
public class ChildComment extends BaseEntity {
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "is_anonymous", nullable = false)
    @ColumnDefault("false")
    private Boolean isAnonymous;

    @Column(name = "tag_user_name")
    private String tagUserName;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @ManyToOne(targetEntity = Comment.class)
    @JoinColumn(name = "parent_comment_id", nullable = false)
    private Comment parentComment;

    public static ChildComment of(
            String content,
            Boolean isDeleted,
            Boolean isAnonymous,
            User writer,
            Comment parentComment
    ) {
        return ChildComment.builder()
                .content(content)
                .isDeleted(isDeleted)
                .isAnonymous(isAnonymous)
                .writer(writer)
                .parentComment(parentComment)
                .build();
    }

    public void delete(){
        this.isDeleted = true;
    }

    public void update(String content){
        this.content = content;
    }
}
