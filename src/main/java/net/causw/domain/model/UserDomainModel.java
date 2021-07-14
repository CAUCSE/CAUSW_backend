package net.causw.domain.model;

import lombok.Getter;
import net.causw.infra.Role;
import net.causw.infra.User;

@Getter
public class UserDomainModel {
    private String id;
    private String email;
    private String name;
    private String password;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
    private Boolean isBlocked;
    private LockerDomainModel locker;

    private UserDomainModel(
            String id,
            String email,
            String name,
            String password,
            Integer admissionYear,
            Role role,
            String profileImage,
            Boolean isBlocked,
            LockerDomainModel locker
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.isBlocked = isBlocked;
        this.locker = locker;
    }

    public static UserDomainModel of(User user) {
        return new UserDomainModel(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getIsBlocked(),
                LockerDomainModel.of(user.getLocker())
        );
    }

}
