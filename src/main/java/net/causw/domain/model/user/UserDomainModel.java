package net.causw.domain.model.user;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.enums.Role;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserDomainModel {
    private String id;
    private String studentId;
    private String profileImage;
    private String refreshToken;

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
    private Role role;

    @NotNull(message = "사용자 상태가 입력되지 않았습니다.")
    private UserState state;

    private UserDomainModel(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Role role,
            String profileImage,
            String refreshToken,
            UserState state
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.refreshToken = refreshToken;
        this.state = state;
    }

    public static UserDomainModel of(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Role role,
            String profileImage,
            String refreshToken,
            UserState state
    ) {
        return new UserDomainModel(
                id,
                email,
                name,
                password,
                studentId,
                admissionYear,
                role,
                profileImage,
                refreshToken,
                state
        );
    }

    public static UserDomainModel of(
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            String profileImage
    ) {
        return new UserDomainModel(
                null,
                email,
                name,
                password,
                studentId,
                admissionYear,
                Role.NONE,
                profileImage,
                null,
                UserState.AWAIT
        );
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
