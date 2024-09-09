package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.enums.UserState;

import java.util.List;
import java.util.Set;

@Getter
@Builder
@Setter
@Entity
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user")
public class User extends BaseEntity {
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = true)  // 일단 null 가능하게 설정(false 로 하면 기존 데이터와 충돌 예상)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "student_id", nullable = true)
    private String studentId;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    // 새로 추가한 필드들
    @Column(name = "nickname",unique = true, nullable = true)
    private String nickname;

    @Column(name = "major", nullable = true)
    private String major;

    @Column(name = "academic_status", nullable = true)
    @Enumerated(EnumType.STRING)
    private AcademicStatus academicStatus;

    @Column(name = "current_completed_semester", nullable = true)
    private Integer currentCompletedSemester;

    @Column(name = "academic_status_note", nullable = true)
    private String academicStatusNote;

    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Column(name = "graduation_month", nullable = true)
    private Integer graduationMonth;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "attach_images", length = 500, nullable = true)
    private List<String> attachImages;

    @Column(name = "profile_image", length = 500, nullable = true)
    private String profileImage;

    @Column(name = "refresh_token", nullable = true)
    private String refreshToken;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserState state;

    @OneToOne
    @JoinColumn(name = "locker_id", nullable = true)
    private Locker locker;

    @OneToMany
    @JoinColumn(name = "user_circle_id", nullable = true)
    private List<CircleMember> circleMemberList;

    private User(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Set<Role> roles,
            String profileImage,
            UserState state
    ) {
        super(id);
        this.email = email;
        this.name = name;
        this.password = password;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.roles = roles;
        this.profileImage = profileImage;
        this.state = state;
    }

    public void delete() {
        this.email = "deleted_" + this.getId();
        this.name = "탈퇴한 사용자";
        this.phoneNumber = null;
        this.studentId = null;
        this.nickname = null;
        this.major = null;
        this.profileImage = null;
        this.graduationYear = null;
        this.graduationMonth = null;
        this.state = UserState.DELETED;
    }

    public static User from(UserDomainModel userDomainModel) {
        return new User(
                userDomainModel.getId(),
                userDomainModel.getEmail(),
                userDomainModel.getName(),
                userDomainModel.getPassword(),
                userDomainModel.getStudentId(),
                userDomainModel.getAdmissionYear(),
                userDomainModel.getRoles(),
                userDomainModel.getProfileImage(),
                userDomainModel.getState()
        );
    }

    public void update(String nickname, AcademicStatus academicStatus, String profileImage) {
        this.nickname = nickname;
        this.academicStatus = academicStatus;
        this.profileImage = profileImage;
    }
}
