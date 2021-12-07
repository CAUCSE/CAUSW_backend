package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.UserAdmissionLogAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_USER_ADMISSION_LOG")
public class UserAdmissionLog extends BaseEntity {
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "admin_user_email", nullable = false)
    private String adminUserEmail;

    @Column(name = "admin_user_name", nullable = false)
    private String adminUserName;

    @Column(name = "image")
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
