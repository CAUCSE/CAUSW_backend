package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.UserDomainModel;
import net.causw.infra.User;
import net.causw.infra.UserState;

@Getter
@NoArgsConstructor
public class UserDetailDto {
    private String id;
    private String email;
    private String name;
    private Integer admissionYear;
    private String role;
    private String profileImage;
    private UserState state;

    private UserDetailDto(
            String id,
            String email,
            String name,
            Integer admissionYear,
            String role,
            String profileImage,
            UserState state
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.state = state;
    }

    public static UserDetailDto from(User user) {
        return new UserDetailDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAdmissionYear(),
                user.getRole().getValue(),
                user.getProfileImage(),
                user.getState()
        );
    }

    public static UserDetailDto from(UserDomainModel user) {
        return new UserDetailDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                UserState.valueOf(user.getState().toUpperCase())
        );
    }
}
