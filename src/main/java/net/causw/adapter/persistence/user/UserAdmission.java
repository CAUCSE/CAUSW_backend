package net.causw.adapter.persistence.user;

import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.user.UserAdmissionDomainModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    @Column(name = "image", length = 500, nullable = true)
    private String attachImage;

    @Column(name = "description", nullable = true)
    private String description;

    private UserAdmission(
            String id,
            User user,
            String attachImage,
            String description
    ) {
        super(id);
        this.user = user;
        this.attachImage = attachImage;
        this.description = description;
    }

    public static UserAdmission from(UserAdmissionDomainModel userAdmissionDomainModel) {
        return new UserAdmission(
                userAdmissionDomainModel.getId(),
                User.from(userAdmissionDomainModel.getUser()),
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription()
        );
    }
}
