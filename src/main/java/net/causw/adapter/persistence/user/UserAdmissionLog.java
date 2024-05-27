package net.causw.adapter.persistence.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.UserAdmissionLogAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Entity
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

    public static UserAdmissionLog of(
            String userEmail,
            String userName,
            String adminUserEmail,
            String adminUserName,
            UserAdmissionLogAction action,
            String attachImage,
            String description
    ) {
        return new UserAdmissionLog(
                userEmail,
                userName,
                adminUserEmail,
                adminUserName,
                attachImage,
                description,
                action
        );
    }
}
