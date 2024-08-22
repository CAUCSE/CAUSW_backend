package net.causw.domain.model.user;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Getter
@Builder
public class UserDomainModel {
    private String id;

    private String studentId;

    private String profileImage;

    private String refreshToken;

    private Integer semester;

    @NotBlank(message = "사용자 이름이 입력되지 않았습니다.")
    private String name;

    @Email(message = "잘못된 이메일 형식입니다.")
    @NotNull(message = "이메일이 입력되지 않았습니다.")
    private String email;

    @NotBlank(message = "비밀번호가 입력되지 않았습니다.")
    private String password;

    @NotNull(message = "입학년도가 입력되지 않았습니다.")
    private Integer admissionYear;

    @NotNull(message = "사용자 권한이 입력되지 않았습니다.")
    private Set<Role> roles;

    @NotNull(message = "사용자 상태가 입력되지 않았습니다.")
    private UserState state;

    public static UserDomainModel of(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Set<Role> roles,
            String profileImage,
            String refreshToken,
            UserState state
    ) {
        return UserDomainModel.builder()
                .id(id)
                .email(email)
                .name(name)
                .password(password)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .roles(roles)
                .profileImage(profileImage)
                .refreshToken(refreshToken)
                .state(state)
                .build();
    }

    public static UserDomainModel of(
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            String profileImage
    ) {
        return UserDomainModel.builder()
                .email(email)
                .name(name)
                .password(password)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .profileImage(profileImage)
                .build();
    }

    public void update(
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            String profileImage
    ) {
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.profileImage = profileImage;
    }

    public String updatePassword(String newPassword) {
        this.password = newPassword;
        return this.password;
    }
}
