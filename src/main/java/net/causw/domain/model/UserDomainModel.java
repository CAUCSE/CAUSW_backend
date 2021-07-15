package net.causw.domain.model;

import lombok.Getter;

@Getter
public class UserDomainModel {
    private String id;
    private String email;
    private String name;
    private Integer admissionYear;
    private String role;
    private String profileImage;
    private Boolean isBlocked;

    private UserDomainModel(
            String id,
            String email,
            String name,
            Integer admissionYear,
            String role,
            String profileImage,
            Boolean isBlocked
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.isBlocked = isBlocked;
    }

    public static UserDomainModel of(
            String id,
            String email,
            String name,
            Integer admissionYear,
            String role,
            String profileImage,
            Boolean isBlocked
    ) {
        return new UserDomainModel(
                id,
                email,
                name,
                admissionYear,
                role,
                profileImage,
                isBlocked
        );
    }
}
