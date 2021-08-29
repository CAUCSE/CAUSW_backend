package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.application.dto.UserFullDto;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_USER")
public class User extends BaseEntity {
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "student_id", nullable = true)
    private String studentId;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "profile_image", nullable = true)
    private String profileImage;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserState state;

    @OneToOne
    @JoinColumn(name = "locker_id", nullable = true)
    private Locker locker;

    @OneToMany
    @JoinColumn(name = "user_circle_id", nullable = true)
    private List<UserCircle> userCircleList;

    private User(
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Role role,
            // TODO: String profileImage,
            UserState state
    ) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.role = role;
        this.state = state;
    }

    private User(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Role role,
            // TODO: String profileImage,
            UserState state
    ) {
        super(id);
        this.email = email;
        this.name = name;
        this.password = password;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.role = role;
        this.state = state;
    }

    public static User of(
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Role role,
            // TODO: String profileImage,
            UserState state
    ) {
        return new User(
                email,
                name,
                password,
                studentId,
                admissionYear,
                role,
                state
        );
    }

    public static User of(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Role role,
            // TODO: String profileImage,
            UserState state
    ) {
        return new User(
                id,
                email,
                name,
                password,
                studentId,
                admissionYear,
                role,
                state
        );
    }

    public static User from(UserFullDto userFullDto) {
        return new User(
                userFullDto.getId(),
                userFullDto.getEmail(),
                userFullDto.getName(),
                userFullDto.getPassword(),
                userFullDto.getStudentId(),
                userFullDto.getAdmissionYear(),
                userFullDto.getRole(),
                userFullDto.getState()
        );
    }
}
