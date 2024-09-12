package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_question")
public class Question extends BaseEntity {
    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "is_multiple", nullable = false)
    @ColumnDefault("false")
    private Boolean isMultiple;

    @OneToMany(mappedBy = "question",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    public static Question of(Integer number, String questionText, Boolean isMultiple, List<Option> options, Form form) {
        return Question.builder()
                .number(number)
                .questionText(questionText)
                .isMultiple(isMultiple)
                .options(options)
                .form(form)
                .build();
    }

    public void setIsMultiple(Boolean isMultiple) {
        this.isMultiple = isMultiple;
    }


}
