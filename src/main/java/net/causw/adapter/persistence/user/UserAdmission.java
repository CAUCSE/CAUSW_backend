package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_admission_id", nullable = true)
    private List<UuidFile> userAdmissionAttachImageUuidFileList;

    @Column(name = "description", nullable = true)
    private String description;

    public static UserAdmission of(User requestUser, List<UuidFile> userAdmissionAttachImageUuidFileList, String description) {
        return UserAdmission.builder()
                .user(requestUser)
                .userAdmissionAttachImageUuidFileList(userAdmissionAttachImageUuidFileList)
                .description(description)
                .build();
    }
}
