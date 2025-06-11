package net.causw.adapter.persistence.notification;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TB_CEREMONY_PUSH_NOTIFICATION")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CeremonyNotificationSetting extends BaseEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TB_CEREMONY_SUBSCRIBE_YEAR", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "admission_year")
    @Builder.Default
    private Set<Integer> subscribedAdmissionYears = new HashSet<>();

    @Column(name = "is_notification_active", nullable = false)
    @Builder.Default
    private boolean isNotificationActive = true;

    @Column(name = "is_set_all", nullable = false)
    @Builder.Default
    private boolean isSetAll = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static CeremonyNotificationSetting of(
            Set<Integer> subscribedAdmissionYears,
            boolean isSetAll,
            boolean receivePushNotification,
            User user
    ) {
        return CeremonyNotificationSetting.builder()
                .subscribedAdmissionYears(subscribedAdmissionYears)
                .isSetAll(isSetAll)
                .isNotificationActive(receivePushNotification)
                .user(user)
                .build();
    }

    public void updateIsSetAll(boolean isSetAll) {
        this.isSetAll = isSetAll;
    }

    public void updateIsNotificationActive(boolean isNotificationActive) {
        this.isNotificationActive = isNotificationActive;
    }
    public void toggleNotification() {
        this.isNotificationActive = !this.isNotificationActive;
    }
}
