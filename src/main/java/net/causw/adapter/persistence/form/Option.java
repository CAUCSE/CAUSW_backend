package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_option",
        indexes = {
                @Index(name = "question_id_index", columnList = "question_id")
})
public class Option extends BaseEntity {
    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    public static Option of(Integer number, String text, Question question) {
        return Option.builder()
                .number(number)
                .optionText(text)
                .question(question)
                .build();
    }
}
