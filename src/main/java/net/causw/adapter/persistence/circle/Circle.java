package net.causw.adapter.persistence.circle;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.hibernate.annotations.ColumnDefault;

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

    @OneToOne
    @JoinColumn(name = "leader_id")
    private User leader;



//
//    @OneToMany(mappedBy = "circle", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Form> forms;

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
            User leader
    ) {
        return Circle.builder()
                .name(name)
                .circleMainImageUuidFile(uuidFile)
                .description(description)
                .isDeleted(isDeleted)
                .circleTax(circleTax)
                .recruitMembers(recruitMembers)
                .leader(leader)
                .build();
    }

    public void update(String name, String description, UuidFile circleMainImageUuidFile, Integer circleTax, Integer recruitMembers){
        this.description = description;
        this.name = name;
        this.circleMainImageUuidFile = circleMainImageUuidFile;
        this.circleTax = circleTax;
        this.recruitMembers = recruitMembers;
    }

    public void setLeader(User leader){
        this.leader = leader;
    }

    public void delete(){
        this.isDeleted = true;
        this.leader = null;
    }
}
