package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserDomainModel {
    private String id;
    private String studentId;
    private String profileImage;

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
        // TODO : Remove following -> Default로 Role.NONE 지정
        Role localRole = Role.NONE;
        UserState localUserState = UserState.AWAIT;
        if (email.equals("admin@gmail.com")) {
            localRole = Role.ADMIN;
            localUserState = UserState.ACTIVE;
        }

        return new UserDomainModel(
                null,
                email,
                name,
                password,
                studentId,
                admissionYear,
                localRole,
                profileImage,
                localUserState
        );
    }
}
