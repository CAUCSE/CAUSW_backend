package net.causw.app.main.domain.model.entity.form;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_reply")
public class Reply extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "reply", cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, orphanRemoval = true)
    @Builder.Default
    private List<ReplyQuestion> replyQuestionList = new ArrayList<>();

    public static Reply of(
            Form form,
            User user,
            List<ReplyQuestion> replyQuestionList
    ) {
        return Reply.builder()
                .form(form)
                .user(user)
                .replyQuestionList(replyQuestionList)
                .build();
    }
}
