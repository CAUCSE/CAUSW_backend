package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_profile_uuid_file",
indexes = {
    @Index(name = "idx_user_profile_user_id", columnList = "user_id"),
    @Index(name = "idx_user_profile_uuid_file_id", columnList = "uuid_file_id")
})
public class UserProfileImage extends BaseEntity {

    @Setter(AccessLevel.PUBLIC)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
    public UuidFile uuidFile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static UserProfileImage of(User user, UuidFile uuidFile) {
        return UserProfileImage.builder()
                .user(user)
                .uuidFile(uuidFile)
                .build();
    }

}
