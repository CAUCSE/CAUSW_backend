package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.PostDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_POST")
public class Post extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

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
            String title,
            String content,
            User writer,
            Boolean isDeleted,
            Board board
    ) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isDeleted = isDeleted;
        this.board = board;
    }

    private Post(
            String id,
            String title,
            String content,
            User writer,
            Boolean isDeleted,
            Board board
    ) {
        super(id);
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isDeleted = isDeleted;
        this.board = board;
    }

    public static Post of(
            String title,
            String content,
            User writer,
            Boolean isDeleted,
            Board board
    ) {
        return new Post(
                title,
                content,
                writer,
                isDeleted,
                board
        );
    }

    public static Post of(
            String id,
            String title,
            String content,
            User writer,
            Boolean isDeleted,
            Board board
    ) {
        return new Post(
                id,
                title,
                content,
                writer,
                isDeleted,
                board
        );
    }

    public static Post from(PostDomainModel postDomainModel) {
        return new Post(
                postDomainModel.getId(),
                postDomainModel.getTitle(),
                postDomainModel.getContent(),
                User.from(postDomainModel.getWriter()),
                postDomainModel.getIsDeleted(),
                Board.from(postDomainModel.getBoard())
        );
    }
}
