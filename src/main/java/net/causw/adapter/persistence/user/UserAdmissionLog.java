package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserAdmissionLogAttachImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.domain.model.enums.user.UserAdmissionLogAction;

import java.util.ArrayList;
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

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy = "userAdmissionLog")
    @Builder.Default
    private List<UserAdmissionLogAttachImage> userAdmissionLogAttachImageList = new ArrayList<>();

    @Column(name = "description")
    private String description;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAdmissionLogAction action;

    @Transient
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
        UserAdmissionLog userAdmissionLog = UserAdmissionLog.builder()
                .userEmail(userEmail)
                .userName(userName)
                .adminUserEmail(adminUserEmail)
                .adminUserName(adminUserName)
                .action(action)
                .description(description)
                .rejectReason(rejectReason)
                .build();

        List<UserAdmissionLogAttachImage> userAdmissionLogAttachImageList = userAdmissionLogAttachImageUuidFileList.stream()
                .map(uuidFile -> UserAdmissionLogAttachImage.of(userAdmissionLog, uuidFile))
                .toList();

        userAdmissionLog.setUserAdmissionLogAttachImageList(userAdmissionLogAttachImageList);

        return userAdmissionLog;
    }

    private void setUserAdmissionLogAttachImageList(List<UserAdmissionLogAttachImage> userAdmissionLogAttachImageList) {
        this.userAdmissionLogAttachImageList = userAdmissionLogAttachImageList;
    }
}
