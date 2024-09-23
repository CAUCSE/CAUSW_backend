package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.AcademicStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form")
public class Form extends BaseEntity {

    @Column(name = "title")
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "allowedGrades", nullable = false)
    private Set<Integer> allowedGrades;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "allowed_academic_status", nullable = true)
    @Enumerated(EnumType.STRING)
    private Set<AcademicStatus> allowedAcademicStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @ManyToOne
    @JoinColumn(name = "circle_id")
    private Circle circle;


    public static Form of(
            String title,
            Set<Integer> allowedGrades,
            List<Question> questions,
            Set<AcademicStatus> allowedAcademicStatus,
            User writer,
            Circle circle
    ) {
        return new Form(
                title,
                allowedGrades != null ? allowedGrades : new HashSet<>(),
                questions,
                false,
                allowedAcademicStatus,
                writer,
                circle
        );
    }

    public void update(String title, Set<Integer> allowedGrades, List<Question> questions, Set<AcademicStatus> allowedAcademicStatus) {
        this.title = title;
        this.allowedGrades = allowedGrades;
        this.questions = questions;
        this.allowedAcademicStatus = allowedAcademicStatus;
    }
}
