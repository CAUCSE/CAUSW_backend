package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_user_profile_uuid_file",
indexes = {
    @Index(name = "idx_user_profile_user_id", columnList = "user_id"),
    @Index(name = "idx_user_profile_uuid_file_id", columnList = "uuid_file_id")
})
public class UserProfileImage extends JoinEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private UserProfileImage(User user, UuidFile uuidFile) {
        super(uuidFile);
        this.user = user;
    }

    public static UserProfileImage of(User user, UuidFile uuidFile) {
        return new UserProfileImage(user, uuidFile);
    }

    public UserProfileImage updateUuidFileAndReturnSelf(UuidFile uuidFile) {
        this.uuidFile = uuidFile;
        return this;
    }

}
