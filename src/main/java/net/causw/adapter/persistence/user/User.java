package net.causw.adapter.persistence.user;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.locker.Locker;
import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;
import net.causw.adapter.persistence.uuidFile.joinEntity.UserProfileImage;
import net.causw.adapter.persistence.vote.VoteRecord;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.user.GraduationType;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;

import java.util.HashSet;
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

    @Column(name = "phone_number", unique = true, nullable = false)
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

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private Locker locker;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private CeremonyNotificationSetting ceremonyNotificationSetting;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<CircleMember> circleMemberList;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<VoteRecord> voteRecordList;

    @Column(name = "rejectionOrDropReason",nullable = true)
    private String rejectionOrDropReason;

    @Setter(AccessLevel.PUBLIC)
    @Column(name = "is_v2", nullable = false)
    @Builder.Default
    private Boolean isV2 = true;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tb_user_fcm_token",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "fcm_token_value")
    @Builder.Default
    private Set<String> fcmTokens = new HashSet<>();

    //fixme : db에러로 임시 설정
    @Column(name = "fcm_token")
    private String fcmToken;

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
                .isV2(true)
                .build();
    }

    public void update(String nickname, UserProfileImage userProfileImage, String phoneNumber) {
        this.nickname = nickname;
        this.userProfileImage = userProfileImage;
        this.phoneNumber = phoneNumber;
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
