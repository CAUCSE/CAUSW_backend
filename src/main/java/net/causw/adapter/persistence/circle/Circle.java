package net.causw.adapter.persistence.circle;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "circle_main_image_uuid_file", nullable = true)
    private UuidFile circleMainImageUuidFile;

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

    public static Circle of(
            String name,
            UuidFile circleMainImageUuidFile,
            String description,
            Boolean isDeleted,
            Integer circleTax,
            Integer recruitMembers,
            User leader,
            LocalDateTime recruitEndDate,
            Boolean isRecruit
    ) {
        return Circle.builder()
                .name(name)
                .circleMainImageUuidFile(circleMainImageUuidFile)
                .description(description)
                .isDeleted(isDeleted)
                .circleTax(circleTax)
                .recruitMembers(recruitMembers)
                .leader(leader)
                .recruitEndDate(recruitEndDate)
                .isRecruit(isRecruit)
                .build();
    }

    public void update(String name, String description, UuidFile circleMainImageUuidFile, Integer circleTax, Integer recruitMembers, LocalDateTime recruitEndDate, Boolean isRecruit) {
        this.description = description;
        this.name = name;
        this.circleMainImageUuidFile = circleMainImageUuidFile;
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
