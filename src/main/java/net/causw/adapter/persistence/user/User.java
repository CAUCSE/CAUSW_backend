package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserProfileImage;
import net.causw.adapter.persistence.vote.VoteRecord;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@Setter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_user")
public class User extends BaseEntity {
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", unique = true, nullable = false)  // 일단 null 가능하게 설정(false 로 하면 기존 데이터와 충돌 예상)
    private String phoneNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "student_id", unique = true, nullable = false)
    private String studentId;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    // 새로 추가한 필드들
    @Column(name = "nickname",unique = true, nullable = false)
    private String nickname;

    @Column(name = "major", nullable = false)
    private String major;

    @Column(name = "academic_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AcademicStatus academicStatus;

    @Column(name = "current_completed_semester", nullable = true)
    private Integer currentCompletedSemester;

    @Column(name = "academic_status_note", nullable = true)
    private String academicStatusNote;

    @Column(name = "graduation_year", nullable = true)
    private Integer graduationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "graduation_type", nullable = true)
    private GraduationType graduationType;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles;

    @OneToOne(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "user")
    private UserProfileImage userProfileImage;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserState state;

    @OneToOne
    @JoinColumn(name = "locker_id", nullable = true)
    private Locker locker;

    @OneToMany
    @JoinColumn(name = "user_circle_id", nullable = true)
    private List<CircleMember> circleMemberList;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<VoteRecord> voteRecordList;

    @Column(name = "rejectionOrDropReason",nullable = true)
    private String rejectionOrDropReason;

    public void delete() {
        this.email = "deleted_" + this.getId();
        this.name = "탈퇴한 사용자";
        this.phoneNumber = null;
        this.studentId = null;
        this.nickname = null;
        this.major = null;
        this.userProfileImage = null;
        this.graduationYear = null;
        this.graduationType = null;
        this.state = UserState.DELETED;
    }

    public static User from (
            UserCreateRequestDto userCreateRequestDto,
            String encodedPassword
    ) {
        return User.builder()
                .email(userCreateRequestDto.getEmail())
                .name(userCreateRequestDto.getName())
                .roles(Set.of(Role.NONE))
                .state(UserState.AWAIT)
                .password(encodedPassword)
                .studentId(userCreateRequestDto.getStudentId())
                .admissionYear(userCreateRequestDto.getAdmissionYear())
                .nickname(userCreateRequestDto.getNickname())
                .major(userCreateRequestDto.getMajor())
                .academicStatus(AcademicStatus.UNDETERMINED)
                .phoneNumber(userCreateRequestDto.getPhoneNumber())
                .build();
    }

    public void update(String nickname, UserProfileImage userProfileImage) {
        this.nickname = nickname;
        this.userProfileImage = userProfileImage;
    }

    public void updateRejectionOrDropReason(String reason) {
        this.rejectionOrDropReason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
