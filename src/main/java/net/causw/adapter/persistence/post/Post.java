package net.causw.adapter.persistence.post;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_post")
public class Post extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "post_id", nullable = true)
    private List<UuidFile> attachImageUuidFileList;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "is_anonymous", nullable = false)
    @ColumnDefault("false")
    private Boolean isAnonymous;

    @Column(name = "is_question", nullable = false)
    @ColumnDefault("false")
    private Boolean isQuestion;

    @ManyToOne(targetEntity = Board.class)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    public static Post of(
            String title,
            String content,
            User writer,
            Boolean isDeleted,
            Boolean isAnonymous,
            Boolean isQuestion,
            Board board,
            List<UuidFile> uuidFileList
    ) {
        return Post.builder()
                .title(title)
                .content(content)
                .attachImageUuidFileList(uuidFileList)
                .writer(writer)
                .isDeleted(isDeleted)
                .isAnonymous(isAnonymous)
                .isQuestion(isQuestion)
                .board(board)
                .build();
    }

    public void update(String title, String content, List<UuidFile> attachImageUuidFileList) {
        this.title = title;
        this.content = content;
        this.attachImageUuidFileList = attachImageUuidFileList;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
