package net.causw.domain.model;

import lombok.Getter;

@Getter
public class UserDomainModel {
    private String id;
    private String email;
    private String name;
    private String password;
    private Integer admissionYear;
    private String role;
    private String profileImage;
    private String state;

    private UserDomainModel(
            String id,
            String email,
            String name,
            String password,
            Integer admissionYear,
            String role,
            String profileImage,
            String state
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
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
            Integer admissionYear,
            String role,
            String profileImage,
            String state
    ) {
        return new UserDomainModel(
                id,
                email,
                name,
                password,
                admissionYear,
                role,
                profileImage,
                state
        );
    }

    public boolean validateSignInPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
}
