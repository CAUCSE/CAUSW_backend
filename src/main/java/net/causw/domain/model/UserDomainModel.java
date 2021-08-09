package net.causw.domain.model;

import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class UserDomainModel {
    private String id;
    private String studentId;
    private String profileImage;

    @NotBlank(message = "Name is blank")
    private String name;

    @Email(message = "Invalid email format")
    @NotNull(message = "Email is null")
    private String email;

    @NotBlank(message = "Password is blank")
    private String password;

    @NotNull(message = "Admission Year is null")
    private Integer admissionYear;

    @NotNull(message = "Role is null")
    private Role role;

    @NotNull(message = "User State is null")
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
}
