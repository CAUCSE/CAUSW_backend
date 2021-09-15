package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @ManyToOne(targetEntity = Board.class)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> commentList;

    private Post(String title, String content, Boolean isDeleted) {
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
    }

    private Post(String id, String title, String content, Boolean isDeleted) {
        super(id);
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
    }

    public static Post of(String title, String content, Boolean isDeleted) {
        return new Post(title, content, isDeleted);
    }

    public static Post from(PostDomainModel postDomainModel) {
        return new Post(
                postDomainModel.getId(),
                postDomainModel.getTitle(),
                postDomainModel.getContent(),
                postDomainModel.getIsDeleted()
        );
    }
}
