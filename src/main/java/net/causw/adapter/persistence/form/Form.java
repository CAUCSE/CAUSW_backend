package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.form.QuestionCreateRequestDto;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form")
public class Form extends BaseEntity {

    @Column(name = "title")
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "allowed_grades", nullable = false)
    private Set<Integer> allowedGrades;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

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
            User writer,
            Circle circle
    ) {
        return Form.builder()
                .title(title)
                .allowedGrades(allowedGrades)
                .questions(questions)
                .isDeleted(false)
                .writer(writer)
                .circle(circle)
                .build();
    }

    public void update(String title, Set<Integer> allowedGrades, List<Question> questions) {
        this.title = title;
        this.allowedGrades = allowedGrades;
        this.questions = questions;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
