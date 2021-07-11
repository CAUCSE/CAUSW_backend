package net.causw.application.dto;

import net.causw.domain.model.UserDomainModel;
import net.causw.infra.Role;

public class UserDetailDto {
    private String id;
    private String email;
    private String name;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
    private Boolean isBlocked;

    private UserDetailDto(
            String id,
            String email,
            String name,
            Integer admissionYear,
            Role role,
            String profileImage,
            Boolean isBlocked
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.isBlocked = isBlocked;
    }

    public static UserDetailDto of(UserDomainModel user) {
        return new UserDetailDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getIsBlocked()
        );
    }
}
