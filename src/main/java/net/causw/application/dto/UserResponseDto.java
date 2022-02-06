package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    private String id;
    private String email;
    private String name;
    private String studentId;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
    private UserState state;
    private String circleIdIfLeader;
    private String circleNameIfLeader;

    private UserResponseDto(
            String id,
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            Role role,
            String profileImage,
            UserState state,
            String circleIdIfLeader,
            String circleNameIfLeader
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.state = state;
        this.circleIdIfLeader = circleIdIfLeader;
        this.circleNameIfLeader = circleNameIfLeader;
    }

    public static UserResponseDto from(UserDomainModel user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState(),
                null,
                null
        );
    }

    public static UserResponseDto from(
            UserDomainModel user,
            String circleId,
            String circleName
    ) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState(),
                circleId,
                circleName
        );
    }
}
