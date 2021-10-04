package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.BoardDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_BOARD")
public class Board extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "create_role_list", nullable = false)
    private String createRoles;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "circle_id")
    private Circle circle;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private Set<Post> postSet;

    private Board(
            String id,
            String name,
            String description,
            String createRoles,
            Boolean isDeleted,
            Circle circle
    ) {
        super(id);
        this.name = name;
        this.description = description;
        this.createRoles = createRoles;
        this.isDeleted = isDeleted;
        this.circle = circle;
    }

    private Board(
            String name,
            String description,
            String createRoles,
            Boolean isDeleted,
            Circle circle
    ) {
        this.name = name;
        this.description = description;
        this.createRoles = createRoles;
        this.isDeleted = isDeleted;
        this.circle = circle;
    }

    public static Board of(
            String id,
            String name,
            String description,
            String createRoles,
            Boolean isDeleted,
            Circle circle
    ) {
        return new Board(
                id,
                name,
                description,
                createRoles,
                isDeleted,
                circle
        );
    }

    public static Board of(
            String name,
            String description,
            String createRoles,
            Boolean isDeleted,
            Circle circle
    ) {
        return new Board(
                name,
                description,
                createRoles,
                isDeleted,
                circle
        );
    }

    public static Board from(BoardDomainModel boardDomainModel) {
        Circle circle = boardDomainModel.getCircle().map(Circle::from).orElse(null);

        return new Board(
                boardDomainModel.getId(),
                boardDomainModel.getName(),
                boardDomainModel.getDescription(),
                String.join(",", boardDomainModel.getCreateRoleList()),
                boardDomainModel.getIsDeleted(),
                circle
        );
    }
}
