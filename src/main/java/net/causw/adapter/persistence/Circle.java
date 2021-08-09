package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import java.util.List;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_CIRCLE")
public class Circle extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "main_image", nullable = true)
    private String mainImage;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @OneToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    @OneToMany
    @JoinColumn(name = "user_circle_id")
    private List<UserCircle> userCircleList;

    private Circle(
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            User leader
    ) {
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leader = leader;
    }

    public static Circle of(
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            User leader
    ) {
        return new Circle(
                name,
                mainImage,
                description,
                isDeleted,
                leader
        );
    }
}
