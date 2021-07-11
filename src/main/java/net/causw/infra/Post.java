package net.causw.infra;

import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(name = "title")
    private String title;

    @Column(columnDefinition = "TEXT", name = "content")
    private String content;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(targetEntity = Board.class)
    @JoinColumn(name = "board_id")
    private Board board;

    private Post(String title, String content, Boolean isDeleted) {
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
    }

    public static Post of(String title, String content, Boolean isDeleted) {
        return new Post(title, content, isDeleted);
    }
}
