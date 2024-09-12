package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
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
        return new Question(number, questionText, isMultiple != null ? isMultiple : false, options, form);
    }

    public void setIsMultiple(Boolean isMultiple) {
        this.isMultiple = isMultiple;
    }


}
