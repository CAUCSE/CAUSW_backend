package net.causw.adapter.persistence.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.user.UserAdmissionDomainModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
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

    public static UserAdmission of(
            User user,
            String attachImage,
            String description
    ) {
        return new UserAdmission(
                null,
                user,
                attachImage,
                description
        );
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
