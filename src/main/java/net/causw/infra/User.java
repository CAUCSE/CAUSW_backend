package net.causw.infra;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_USER")
public class User extends BaseEntity {
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "admission_year")
    private Integer admissionYear;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "profile_image", nullable = true)
    private String profileImage;

    @Column(name = "is_certified")
    private Boolean isCertified;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    // TODO: Add Foreign Key

    private User(
            String email,
            String name,
            String password,
            Integer admissionYear,
            Role role,
            String profileImage,
            Boolean isCertified,
            Boolean isBlocked
    ) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.admissionYear = admissionYear;
        this.role = role;
        this.profileImage = profileImage;
        this.isCertified = isCertified;
        this.isBlocked = isBlocked;
    }

    public static User of(
            String email,
            String name,
            String password,
            Integer admissionYear,
            Role role,
            String profileImage,
            Boolean isCertified,
            Boolean isBlocked
    ) {
        return new User(email, name, password, admissionYear, role, profileImage, isCertified, isBlocked);
    }
}
