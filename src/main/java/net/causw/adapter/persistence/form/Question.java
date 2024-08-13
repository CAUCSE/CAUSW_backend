package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_question")
public class Question extends BaseEntity {

    @Column(name = "text", nullable = false)
    private String text;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "type", nullable = false)
//    private QuestionType type;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    public static Question of(String text,  Form form) {
        return new Question(text, form);
    }
}
