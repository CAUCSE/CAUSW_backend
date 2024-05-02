package net.causw.adapter.persistence.comment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.comment.ChildCommentDomainModel;
import net.causw.domain.model.post.PostDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_child_comment")
public class ChildComment extends BaseEntity {
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "tag_user_name")
    private String tagUserName;

    @Column(name = "ref_child_comment")
    private String refChildComment;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @ManyToOne(targetEntity = Comment.class)
    @JoinColumn(name = "parent_comment_id", nullable = false)
    private Comment parentComment;

    private ChildComment(
            String id,
            String content,
            Boolean isDeleted,
            String tagUserName,
            String refChildComment,
            User writer,
            Comment parentComment
    ) {
        super(id);
        this.content = content;
        this.isDeleted = isDeleted;
        this.tagUserName = tagUserName;
        this.refChildComment = refChildComment;
        this.writer = writer;
        this.parentComment = parentComment;
    }


    public static ChildComment from(
            ChildCommentDomainModel childCommentDomainModel,
            PostDomainModel postDomainModel
    ) {
        return new ChildComment(
                childCommentDomainModel.getId(),
                childCommentDomainModel.getContent(),
                childCommentDomainModel.getIsDeleted(),
                childCommentDomainModel.getTagUserName(),
                childCommentDomainModel.getRefChildComment(),
                User.from(childCommentDomainModel.getWriter()),
                Comment.from(childCommentDomainModel.getParentComment(), postDomainModel)
        );
    }

    public void delete(){
        this.isDeleted = true;
    }

    public void update(String content, String tagUserName, String refChildComment){
        this.content = content;
        this.tagUserName = tagUserName;
        this.refChildComment = refChildComment;
    }
}
