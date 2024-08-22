package net.causw.adapter.persistence.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_reply")
public class Reply extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "question_answer")
    private String questionAnswer;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "selected_option")
    private List<Integer> selectedOptions;

    public static Reply of(Form form, User user, Question question, String questionAnswer, List<Integer> selectedOptions) {
        return new Reply(form, user, question, questionAnswer, selectedOptions);
    }
}
