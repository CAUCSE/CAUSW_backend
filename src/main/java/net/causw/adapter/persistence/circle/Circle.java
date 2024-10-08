package net.causw.adapter.persistence.circle;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.joinEntity.CircleMainImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_circle")
public class Circle extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Setter(value = AccessLevel.PROTECTED)
    @OneToOne(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "circle")
    private CircleMainImage circleMainImage;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @Column(name = "circle_tax")
    private Integer circleTax;

    @Column(name = "recruit_members")
    private Integer recruitMembers;

    @Setter
    @Column(name = "recruit_end_date")
    private LocalDateTime recruitEndDate;

    @Setter
    @Column(name = "is_recruit")
    @Builder.Default
    private Boolean isRecruit = false;

    @Setter
    @OneToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    public Optional<User> getLeader() {
        return Optional.ofNullable(this.leader);
    }

    public static Circle of(
            String name,
            UuidFile uuidFile,
            String description,
            Boolean isDeleted,
            Integer circleTax,
            Integer recruitMembers,
            User leader,
            LocalDateTime recruitEndDate,
            Boolean isRecruit
    ) {
        Circle circle = Circle.builder()
                .name(name)
                .description(description)
                .isDeleted(isDeleted)
                .circleTax(circleTax)
                .recruitMembers(recruitMembers)
                .leader(leader)
                .recruitEndDate(recruitEndDate)
                .isRecruit(isRecruit)
                .build();

        if (uuidFile == null) {
            return circle;
        }

        CircleMainImage circleMainImage = CircleMainImage.of(
                circle,
                uuidFile
        );

        circle.setCircleMainImage(circleMainImage);

        return circle;
    }

    public void update(String name, String description, CircleMainImage circleMainImage, Integer circleTax, Integer recruitMembers, LocalDateTime recruitEndDate, Boolean isRecruit) {
        this.description = description;
        this.name = name;
        this.circleMainImage = circleMainImage;
        this.circleTax = circleTax;
        this.recruitMembers = recruitMembers;
        this.recruitEndDate = recruitEndDate;
        this.isRecruit = isRecruit;
    }

    public void delete(){
        this.isDeleted = true;
        this.leader = null;
    }

}
