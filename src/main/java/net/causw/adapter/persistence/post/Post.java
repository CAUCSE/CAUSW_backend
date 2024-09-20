package net.causw.adapter.persistence.post;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.uuidFile.joinEntity.PostAttachImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
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

    @OneToMany(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "post")
    @Builder.Default
    private List<PostAttachImage> postAttachImageList = new ArrayList<>();

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
            List<UuidFile> postAttachImageUuidFileList
    ) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .isDeleted(isDeleted)
                .isAnonymous(isAnonymous)
                .isQuestion(isQuestion)
                .board(board)
                .build();

        if (postAttachImageUuidFileList.isEmpty()) {
            return post;
        }

        List<PostAttachImage> postAttachImageList = postAttachImageUuidFileList.stream()
                .map(uuidFile -> PostAttachImage.of(post, uuidFile))
                .toList();

        post.setPostAttachFileList(postAttachImageList);

        return post;
    }

    public void update(String title, String content, List<PostAttachImage> postAttachImageList) {
        this.title = title;
        this.content = content;
        this.postAttachImageList = postAttachImageList;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    private void setPostAttachFileList(List<PostAttachImage> postAttachImageList) {
        this.postAttachImageList = postAttachImageList;
    }
}
