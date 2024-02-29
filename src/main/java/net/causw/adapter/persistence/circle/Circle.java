package net.causw.adapter.persistence.circle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.circle.CircleDomainModel;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Optional;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "tb_circle")
public class Circle extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "main_image", length = 500, nullable = true)
    private String mainImage;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @OneToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    public Optional<User> getLeader() {
        return Optional.ofNullable(this.leader);
    }

    private Circle(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            User leader
    ) {
        super(id);
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.isDeleted = isDeleted;
        this.leader = leader;
    }

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

    public static Circle of(
            String id,
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            User leader
    ) {
        return new Circle(
                id,
                name,
                mainImage,
                description,
                isDeleted,
                leader
        );
    }

    public static Circle from(CircleDomainModel circleDomainModel) {
        return new Circle(
                circleDomainModel.getId(),
                circleDomainModel.getName(),
                circleDomainModel.getMainImage(),
                circleDomainModel.getDescription(),
                circleDomainModel.getIsDeleted(),
                circleDomainModel.getLeader().map(User::from).orElse(null)
        );
    }
}
