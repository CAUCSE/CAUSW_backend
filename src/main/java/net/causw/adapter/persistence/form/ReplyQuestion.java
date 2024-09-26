package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_reply_question",
        indexes = {
                @Index(name = "reply_id_index", columnList = "reply_id"),
                @Index(name = "form_question_id_index", columnList = "form_question_id")
})
public class ReplyQuestion extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_question_id", nullable = false)
    private FormQuestion formQuestion;

    @Lob // 텍스트 대용량 처리
    @Column(name = "question_answer", columnDefinition = "TEXT")
    private String questionAnswer;

    @Column(name = "selected_option_list", nullable = true)
    private String selectedOptionList;

    public static ReplyQuestion of(
            FormQuestion formQuestion,
            String questionAnswer,
            List<Integer> selectedOptionList
    ) {
        return ReplyQuestion.builder()
                .formQuestion(formQuestion)
                .questionAnswer(questionAnswer)
                .selectedOptionList(ReplySelectedOptionManager.fromIntegerList(selectedOptionList).serialize())
                .build();
    }

    public List<Integer> getSelectedOptionList() {
        return ReplySelectedOptionManager.fromString(this.selectedOptionList).getSelectedOptionList();
    }

}
