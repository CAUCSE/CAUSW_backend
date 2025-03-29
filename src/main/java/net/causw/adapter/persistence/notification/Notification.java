package net.causw.adapter.persistence.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.notification.NoticeType;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_notification")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "notice_type")
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Column(name = "target_id")
    private String targetId;

    public static Notification of(
            User user,
            String title,
            String body,
            NoticeType noticeType,
            String targetId
    ) {
        return Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .noticeType(noticeType)
                .targetId(targetId)
                .build();
    }

}
