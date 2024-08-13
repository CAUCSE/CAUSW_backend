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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form")
public class Form extends BaseEntity {

    @Column(name = "title")
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "allowedGrades", nullable = false)
    private Set<Integer> allowedGrades;


    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;  // 작성자 정보

//    @ManyToOne
//    @JoinColumn(name = "circle_id", nullable = true)
//    private Circle circle;


    private Form(
            String id,
            String title,
            Set<Integer> allowedGrades,
            List<Question> questions,
            Boolean isDeleted,
            User writer
//            Circle circle
    ) {
        super(id);
        this.title = title;
        this.allowedGrades = allowedGrades;
        this.questions = questions;
        this.isDeleted = isDeleted;
        this.writer = writer;
//        this.circle = circle;
    }

    public static Form of(
            String title,
            Set<Integer> allowedGrades,
            List<Question> questions,
            User writer
//            Circle circle
    ) {
        return new Form(
                null, // ID는 자동 생성되므로 null로 설정
                title,
                allowedGrades != null ? allowedGrades : new HashSet<>(),
                questions,
                false,
                writer
//                circle
        );
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
