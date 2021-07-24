package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.adapter.persistence.User;
import net.causw.domain.model.UserState;

@Getter
@NoArgsConstructor
public class UserDetailDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
    private UserState state;

    private UserDetailDto(
            String id,
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            Role role,
            String profileImage,
            UserState state
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
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
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState()
        );
    }

    public static UserDetailDto from(UserDomainModel user) {
        return new UserDetailDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState()
        );
    }
}
