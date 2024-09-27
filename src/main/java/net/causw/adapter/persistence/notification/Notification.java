package net.causw.adapter.persistence.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.NoticeType;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_notification")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "content")
    private String content;

    @Column(name = "notice_type")
    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    @Column(name = "is_global")
    @ColumnDefault("false")
    private Boolean isGlobal;

    public static Notification of(
            User user,
            String content,
            NoticeType noticeType,
            Boolean isGlobal
    ) {
        return Notification.builder()
                .user(user)
                .content(content)
                .noticeType(noticeType)
                .isGlobal(isGlobal)
                .build();
    }

}
