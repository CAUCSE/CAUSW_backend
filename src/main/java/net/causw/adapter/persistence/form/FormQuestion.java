package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.form.QuestionType;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form_question",
        indexes = {
        @Index(name = "form_id_index", columnList = "form_id")
})
public class FormQuestion extends BaseEntity {

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(name = "is_multiple", nullable = false)
    @ColumnDefault("false")
    private Boolean isMultiple;

    @OneToMany(mappedBy = "formQuestion", cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, orphanRemoval = true)
    @Builder.Default
    private List<FormQuestionOption> formQuestionOptionList = new ArrayList<>();

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    public static FormQuestion of(Integer number, QuestionType questionType, String questionText, Boolean isMultiple, List<FormQuestionOption> formQuestionOptionList, Form form) {
        return FormQuestion.builder()
                .number(number)
                .questionType(questionType)
                .questionText(questionText)
                .isMultiple(isMultiple)
                .formQuestionOptionList(formQuestionOptionList)
                .form(form)
                .build();
    }


}
