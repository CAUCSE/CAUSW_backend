package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.UserDomainModel;
import net.causw.infra.Locker;
import net.causw.infra.Role;

@Getter
@NoArgsConstructor
public class UserDetailDto {
    private String id;
    private String email;
    private String name;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
    private Boolean isBlocked;
    private Locker locker;

    private UserDetailDto(
            String id,
            String email,
            String name,
            Integer admissionYear,
            Role role,
            String profileImage,
            Boolean isBlocked,
            Locker locker
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.isBlocked = isBlocked;
        this.locker = locker;
    }

    public static UserDetailDto of(UserDomainModel user) {
        return new UserDetailDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getIsBlocked(),
                user.getLocker()
        );
    }
}
