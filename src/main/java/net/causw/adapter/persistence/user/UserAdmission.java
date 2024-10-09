package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserAdmissionAttachImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_admission")
public class UserAdmission extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, mappedBy = "userAdmission")
    @Builder.Default
    private List<UserAdmissionAttachImage> userAdmissionAttachImageList = new ArrayList<>();

    @Column(name = "description", nullable = true)
    private String description;

    public static UserAdmission of(User requestUser, List<UuidFile> userAdmissionAttachImageUuidFileList, String description) {
        UserAdmission userAdmission = UserAdmission.builder()
                .user(requestUser)
                .description(description)
                .build();

        List<UserAdmissionAttachImage> userAdmissionAttachImageList = userAdmissionAttachImageUuidFileList.stream()
                .map(uuidFile -> UserAdmissionAttachImage.of(userAdmission, uuidFile))
                .toList();

        userAdmission.setUserAdmissionAttachImageList(userAdmissionAttachImageList);

        return userAdmission;
    }

    private void setUserAdmissionAttachImageList(List<UserAdmissionAttachImage> userAdmissionAttachImageList) {
        this.userAdmissionAttachImageList = userAdmissionAttachImageList;
    }
}
