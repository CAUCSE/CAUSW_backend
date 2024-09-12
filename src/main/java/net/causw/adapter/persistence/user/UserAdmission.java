package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.domain.model.user.UserAdmissionDomainModel;

import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user_admission")
public class UserAdmission extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_admission_id", nullable = true)
    private List<UuidFile> uuidFileList;

    @Column(name = "description", nullable = true)
    private String description;

    private UserAdmission(
            String id,
            User user,
            List<UuidFile> uuidFileList,
            String description
    ) {
        super(id);
        this.user = user;
        this.uuidFileList = uuidFileList;
        this.description = description;
    }

    public static UserAdmission from(UserAdmissionDomainModel userAdmissionDomainModel) {
        return new UserAdmission(
                userAdmissionDomainModel.getId(),
                User.from(userAdmissionDomainModel.getUser()),
                userAdmissionDomainModel.getUuidFileList(),
                userAdmissionDomainModel.getDescription()
        );
    }
}
