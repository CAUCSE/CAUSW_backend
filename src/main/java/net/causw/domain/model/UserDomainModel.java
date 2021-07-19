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
    private Boolean isBlocked;

    private UserDomainModel(
            String id,
            String email,
            String name,
            String password,
            Integer admissionYear,
            String role,
            String profileImage,
            Boolean isBlocked
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.isBlocked = isBlocked;
    }

    public static UserDomainModel of(
            String id,
            String email,
            String name,
            String password,
            Integer admissionYear,
            String role,
            String profileImage,
            Boolean isBlocked
    ) {
        return new UserDomainModel(
                id,
                email,
                name,
                password,
                admissionYear,
                role,
                profileImage,
                isBlocked
        );
    }

    public boolean validateSignInPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }
}
