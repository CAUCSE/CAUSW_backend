package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(name = "modify_role_list", nullable = false)
    private String modifyRoles;

    @Column(name = "read_role_list", nullable = false)
    private String readRoles;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "circle_id")
    private Circle circle;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private Set<Post> postSet;

    private Board(
            String name,
            String description,
            String createRoles,
            String modifyRoles,
            String readRoles,
            Boolean isDeleted,
            Circle circle
    ) {
        this.name = name;
        this.description = description;
        this.createRoles = createRoles;
        this.modifyRoles = modifyRoles;
        this.readRoles = readRoles;
        this.isDeleted = isDeleted;
        this.circle = circle;
    }

    public static Board of(
            String name,
            String description,
            String createRoles,
            String modifyRoles,
            String readRoles,
            Boolean isDeleted,
            Circle circle
    ) {
        return new Board(
                name,
                description,
                createRoles,
                modifyRoles,
                readRoles,
                isDeleted,
                circle
        );
    }
}
