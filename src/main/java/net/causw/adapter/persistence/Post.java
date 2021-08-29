package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.application.dto.BoardDetailDto;
import net.causw.application.dto.PostFullDto;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_POST")
public class Post extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @ManyToOne(targetEntity = Board.class)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> commentList;

    private Post(
            String title,
            String content,
            Boolean isDeleted,
            User writer,
            Board board
    ) {
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.writer = writer;
        this.board = board;
    }

    private Post(
            String id,
            String title,
            String content,
            Boolean isDeleted,
            User writer,
            Board board
    ) {
        super(id);
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
        this.writer = writer;
        this.board = board;
    }

    public static Post of(
            String title,
            String content,
            Boolean isDeleted,
            User writer,
            Board board
    ) {
        return new Post(
                title,
                content,
                isDeleted,
                writer,
                board
        );
    }

    public static Post from(PostFullDto postFullDto) {
        BoardDetailDto boardDetailDto = postFullDto.getBoard();
        return new Post(
                postFullDto.getId(),
                postFullDto.getTitle(),
                postFullDto.getContent(),
                postFullDto.getIsDeleted(),
                User.from(postFullDto.getWriter()),
                Board.of(
                        boardDetailDto.getName(),
                        boardDetailDto.getDescription(),
                        boardDetailDto.getCreateRoleList().stream().collect(Collectors.joining(",")),
                        boardDetailDto.getModifyRoleList().stream().collect(Collectors.joining(",")),
                        boardDetailDto.getReadRoleList().stream().collect(Collectors.joining(",")),
                        boardDetailDto.getIsDeleted()
                )
        );
    }
}
