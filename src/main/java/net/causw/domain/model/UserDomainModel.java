package net.causw.domain.model;

import lombok.Getter;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class UserDomainModel {
    private String id;
    private String email;
    private String name;
    private String password;
    private String studentId;
    private Integer admissionYear;
    private Role role;
    private String profileImage;
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

    public boolean validateSignInPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public boolean validateSignUpPassword() {
        String passwordPolicy = "((?=.*[a-z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,})";

        Pattern pattern_password = Pattern.compile(passwordPolicy);
        Matcher matcher_password = pattern_password.matcher(this.password);

        return matcher_password.matches();
    }

    public boolean validateSignUpAdmissionYear() {
        if (this.admissionYear < 1972) { return false; }

        Calendar cal = Calendar.getInstance();
        int presentYear = cal.get(Calendar.YEAR);
        return this.admissionYear <= presentYear;
    }
}
