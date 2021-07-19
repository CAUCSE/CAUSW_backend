package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.infra.Role;
import net.causw.infra.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UserFullDto {
    private String id;
    private String email;
    private String name;
    private String password;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
    private Boolean isBlocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserFullDto(
            String id,
            String email,
            String name,
            String password,
            Integer admissionYear,
            Role role,
            String profileImage,
            Boolean isBlocked,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.isBlocked = isBlocked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserFullDto from(User user) {
        return new UserFullDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getAdmissionYear(),
                user.getRole(),
                user.getProfileImage(),
                user.getIsBlocked(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
