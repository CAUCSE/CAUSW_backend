package net.causw.adapter.persistence.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.UserAdmissionLogAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
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

    private UserAdmissionLog(
            String id,
            String userEmail,
            String userName,
            String adminUserEmail,
            String adminUserName,
            UserAdmissionLogAction action,
            String attachImage,
            String description
    ) {
        super(id);
        this.userEmail = userEmail;
        this.userName = userName;
        this.adminUserEmail = adminUserEmail;
        this.adminUserName = adminUserName;
        this.action = action;
        this.attachImage = attachImage;
        this.description = description;
    }

    private UserAdmissionLog(
            String userEmail,
            String userName,
            String adminUserEmail,
            String adminUserName,
            UserAdmissionLogAction action,
            String attachImage,
            String description
    ) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.adminUserEmail = adminUserEmail;
        this.adminUserName = adminUserName;
        this.action = action;
        this.attachImage = attachImage;
        this.description = description;
    }

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
                action,
                attachImage,
                description
        );
    }
}
