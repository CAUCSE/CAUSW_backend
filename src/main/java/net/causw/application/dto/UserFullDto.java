package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.Role;
import net.causw.adapter.persistence.User;
import net.causw.domain.model.UserState;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UserFullDto {
    private String id;
    private String email;
    private String name;
    private String password;
    private String studentId;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
    private UserState state;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserFullDto(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Role role,
            String profileImage,
            UserState state,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.state = state;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserFullDto from(User user) {
        return new UserFullDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getStudentId(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getState(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
