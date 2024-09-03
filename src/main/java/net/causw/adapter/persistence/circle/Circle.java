package net.causw.adapter.persistence.circle;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.form.Question;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.circle.CircleDomainModel;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;
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

    @OneToOne
    @JoinColumn(name = "leader_id")
    private User leader;



//
//    @OneToMany(mappedBy = "circle", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Form> forms;

    public Optional<User> getLeader() {
        return Optional.ofNullable(this.leader);
    }

    private Circle(
            String id,
            String name,
            String mainImage,
            String description,
            Integer circleTax,
            Integer recuritMembers,
            Boolean isDeleted,
            User leader
    ) {
        super(id);
        this.name = name;
        this.mainImage = mainImage;
        this.description = description;
        this.circleTax = circleTax;
        this.recruitMembers = recuritMembers;
        this.isDeleted = isDeleted;
        this.leader = leader;
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
                circleDomainModel.getLeader().map(User::from).orElse(null)
        );
    }

    public static Circle of(
            String name,
            String mainImage,
            String description,
            Boolean isDeleted,
            Integer circleTax,
            Integer recuritMembers,
            User leader
    ) {
        return new Circle(name, mainImage, description, isDeleted, circleTax, recuritMembers, leader);
    }

    public void update(String name, String description, String mainImage, Integer circleTax, Integer recuritMembers){
        this.description = description;
        this.name = name;
        this.mainImage = mainImage;
        this.circleTax = circleTax;
        this.recruitMembers = recuritMembers;
    }

    public void setLeader(User leader){
        this.leader = leader;
    }

    public void delete(){
        this.isDeleted = true;
        this.leader = null;
    }
}
