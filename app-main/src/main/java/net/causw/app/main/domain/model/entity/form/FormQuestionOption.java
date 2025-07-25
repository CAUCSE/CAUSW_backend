package net.causw.app.main.domain.model.entity.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_form_question_option",
        indexes = {
                @Index(name = "form_question_id_index", columnList = "form_question_id")
})
public class FormQuestionOption extends BaseEntity {

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_question_id", nullable = false)
    private FormQuestion formQuestion;

    public static FormQuestionOption of(Integer number, String text, FormQuestion formQuestion) {
        return FormQuestionOption.builder()
                .number(number)
                .optionText(text)
                .formQuestion(formQuestion)
                .build();
    }

    public void setFormQuestion(FormQuestion formQuestion) {
        this.formQuestion = formQuestion;
    }
}
