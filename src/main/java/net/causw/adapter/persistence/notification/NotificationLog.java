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
@Table(name = "tb_notification_log")
public class NotificationLog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    public static NotificationLog of(
            User user,
            Notification notification
    ) {
        return NotificationLog.builder()
                .user(user)
                .notification(notification)
                .build();
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

}
