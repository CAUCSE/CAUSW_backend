package net.causw.adapter.db;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

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

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private UserState state;

    @OneToOne
    @JoinColumn(name = "circle_id", nullable = true)
    private Circle managingCircle;

    private User(
            String email,
            String name,
            String password,
            Integer admissionYear,
            Role role,
            // TODO: String profileImage,
            UserState state
    ) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.admissionYear = admissionYear;
        this.role = role;
        this.state = state;
    }

    public static User of(
            String email,
            String name,
            String password,
            Integer admissionYear,
            Role role,
            // TODO: String profileImage,
            UserState state
    ) {
        return new User(
                email,
                name,
                password,
                admissionYear,
                role,
                state
        );
    }
}
