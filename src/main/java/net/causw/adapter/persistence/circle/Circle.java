package net.causw.adapter.persistence.circle;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.circle.CircleDomainModel;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(name = "circle_tax")
    private Integer circleTax;

    @Column(name = "recruit_members")
    private Integer recruitMembers;

    @Column(name = "recruit_end_date")
    private LocalDateTime recruitEndDate;

    @Column(name = "is_recruit")
    @ColumnDefault("false")
    private Boolean isRecruit;

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
            Integer circleTax,
            Integer recruitMembers,
            Boolean isDeleted,
            User leader,
            LocalDateTime recruitEndDate,
            Boolean isRecruit
    ) {
        super(id);
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.circleTax = circleTax;
        this.recruitMembers = recruitMembers;
        this.isDeleted = isDeleted;
        this.leader = leader;
        this.recruitEndDate = recruitEndDate;
        this.isRecruit = isRecruit;
    }

    public static Circle from(CircleDomainModel circleDomainModel) {
        return new Circle(
                circleDomainModel.getId(),
                circleDomainModel.getName(),
                circleDomainModel.getMainImage(),
                circleDomainModel.getDescription(),
                circleDomainModel.getCircleTax(),
                circleDomainModel.getRecruitMembers(),
                circleDomainModel.getIsDeleted(),
                circleDomainModel.getLeader().map(User::from).orElse(null),
                circleDomainModel.getRecruitEndDate(),
                circleDomainModel.getIsRecruit()
        );
    }

    public static Circle of(
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            Integer circleTax,
            Integer recruitMembers,
            User leader,
            LocalDateTime recruitEndDate,
            Boolean isRecruit
    ) {
        return new Circle(name, mainImage, description, isDeleted, circleTax, recruitMembers, recruitEndDate, isRecruit, leader);
    }

    public void update(String name, String description, String mainImage, Integer circleTax, Integer recruitMembers, LocalDateTime recruitEndDate, Boolean isRecruit) {
        this.description = description;
        this.name = name;
        this.mainImage = mainImage;
        this.circleTax = circleTax;
        this.recruitMembers = recruitMembers;
        this.recruitEndDate = recruitEndDate;
        this.isRecruit = isRecruit;
    }

    public void setLeader(User leader){
        this.leader = leader;
    }

    public void delete(){
        this.isDeleted = true;
        this.leader = null;
    }
}
