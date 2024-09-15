package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.domain.model.enums.UserAdmissionLogAction;

import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_admission_log_id", nullable = true)
    private List<UuidFile> userAdmissionLogAttachImageUuidFileList;

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
            List<UuidFile> userAdmissionLogAttachImageUuidFileList,
            String description,
            String rejectReason
    ) {
        return UserAdmissionLog.builder()
                .userEmail(userEmail)
                .userName(userName)
                .adminUserEmail(adminUserEmail)
                .adminUserName(adminUserName)
                .action(action)
                .userAdmissionLogAttachImageUuidFileList(userAdmissionLogAttachImageUuidFileList)
                .description(description)
                .rejectReason(rejectReason)
                .build(
        );
    }
}
