package net.causw.app.main.domain.model.entity.circle;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.form.Form;
import net.causw.app.main.domain.model.entity.form.Reply;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.circle.CircleMemberStatus;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_circle_member")
public class CircleMember extends BaseEntity {

    @Setter
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CircleMemberStatus status;

    @ManyToOne
    @JoinColumn(name = "circle_id", nullable = false)
    private Circle circle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 신청 당시 제출한 신청서
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = true)
    private Form appliedForm;

    // 신청 당시 제출한 신청서 답변
    @Setter
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "reply_id", nullable = true)
    private Reply appliedReply;

    public static CircleMember of(
            Circle circle,
            User user,
            Form form,
            Reply reply
    ) {
        return CircleMember.builder()
                .status(CircleMemberStatus.AWAIT)
                .circle(circle)
                .user(user)
                .appliedForm(form)
                .appliedReply(reply)
                .build();
    }

}
