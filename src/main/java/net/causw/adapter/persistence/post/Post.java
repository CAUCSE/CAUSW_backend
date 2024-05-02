package net.causw.adapter.persistence.post;

import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.board.Board;
import net.causw.domain.model.post.PostDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.util.Optional;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_post")
public class Post extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

    @Column(name = "attachments", length = 1500)
    private String attachments;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @ManyToOne(targetEntity = Board.class)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    private Post(
            String id,
            String title,
            String content,
            User writer,
            Boolean isDeleted,
            Board board,
            String attachments
    ) {
        super(id);
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isDeleted = isDeleted;
        this.board = board;
        this.attachments = attachments;
    }

    public static Post from(PostDomainModel postDomainModel) {
        return new Post(
                postDomainModel.getId(),
                postDomainModel.getTitle(),
                postDomainModel.getContent(),
                User.from(postDomainModel.getWriter()),
                postDomainModel.getIsDeleted(),
                Board.from(postDomainModel.getBoard()),
                String.join(":::", postDomainModel.getAttachmentList())
        );
    }

    public static Post of(
            String title,
            String content,
            User writer,
            Boolean isDeleted,
            Board board,
            String attachments
    ) {
        return new Post(title, content, attachments, writer, isDeleted, board);
    }

    public void update(String title, String content, String attachments) {
        this.title = title;
        this.content = content;
        this.attachments = attachments;
    }

    public Optional<String> getAttachments() {
        return Optional.ofNullable(this.attachments);
    }
}
