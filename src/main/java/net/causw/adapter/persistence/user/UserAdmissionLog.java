package net.causw.adapter.persistence.user;

import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.UserAdmissionLogAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_user_admission_log")
public class UserAdmissionLog extends BaseEntity {
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "admin_user_email", nullable = false)
    private String adminUserEmail;

    @Column(name = "admin_user_name", nullable = false)
    private String adminUserName;

    @Column(name = "image", length = 500)
    private String attachImage;

    @Column(name = "description")
    private String description;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAdmissionLogAction action;

    @Column(name = "rejectReason",nullable = true)
    private String rejectReason;

    public static UserAdmissionLog of(
            String userEmail,
            String userName,
            String adminUserEmail,
            String adminUserName,
            UserAdmissionLogAction action,
            String attachImage,
            String description,
            String rejectReason
    ) {
        return new UserAdmissionLog(
                userEmail,
                userName,
                adminUserEmail,
                adminUserName,
                attachImage,
                description,
                action,
                rejectReason
        );
    }
}
