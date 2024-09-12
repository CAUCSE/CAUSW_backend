package net.causw.domain.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.GraduationType;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

@Getter
@Builder
public class UserDomainModel {
    private String id;

    private String studentId;

    private UuidFile uuidFile;

    private String refreshToken;

    @NotBlank(message = "사용자 이름이 입력되지 않았습니다.")
    private String name;

    @Email(message = "잘못된 이메일 형식입니다.")
    @NotNull(message = "이메일이 입력되지 않았습니다.")
    private String email;

    @NotBlank(message = "비밀번호가 입력되지 않았습니다.")
    private String password;

    @NotNull(message = "입학년도가 입력되지 않았습니다.")
    private Integer admissionYear;

    @NotNull(message = "사용자 권한이 입력되지 않았습니다.")
    private Set<Role> roles;

    @NotNull(message = "사용자 상태가 입력되지 않았습니다.")
    private UserState state;

    // 새로 추가된 필드들
    @Schema(description = "닉네임", example = "푸앙")
    private String nickname;

    @Schema(description = "학부/학과", example = "소프트웨어학부")
    private String major;

    @Schema(description = "학적상태", example = "ENROLLED")
    private AcademicStatus academicStatus;

    @Schema(description = "현재 등록 완료된 학기", example = "6(3학년 2학기)")
    private Integer currentCompletedSemester;

    @Schema(description = "졸업시기 년", example = "2026")
    private Integer graduationYear;

    @Schema(description = "졸업시기 월", example = "2")
    private GraduationType graduationType;

    @Schema(description = "전화번호", example = "01012345678")
    private String phoneNumber;

    public static UserDomainModel of(
            String id,
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            Set<Role> roles,
            UuidFile uuidFile,
            String refreshToken,
            UserState state,
            String nickname,
            String major,
            AcademicStatus academicStatus,
            Integer currentCompletedSemester,
            Integer graduationYear,
            GraduationType graduationType,
            String phoneNumber
    ) {
        return UserDomainModel.builder()
                .id(id)
                .email(email)
                .name(name)
                .password(password)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .roles(roles)
                .uuidFile(uuidFile)
                .refreshToken(refreshToken)
                .state(state)
                .nickname(nickname)
                .major(major)
                .academicStatus(academicStatus)
                .currentCompletedSemester(currentCompletedSemester)
                .graduationYear(graduationYear)
                .graduationType(graduationType)
                .phoneNumber(phoneNumber)
                .build();
    }

    public static UserDomainModel of(
            String email,
            String name,
            String password,
            String studentId,
            Integer admissionYear,
            UuidFile uuidFile,
            String nickname,
            String major,
            AcademicStatus academicStatus,
            Integer currentCompletedSemester,
            Integer graduationYear,
            GraduationType graduationType,
            String phoneNumber
    ) {
        return UserDomainModel.builder()
                .email(email)
                .name(name)
                .password(password)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .uuidFile(uuidFile)
                .nickname(nickname)
                .major(major)
                .academicStatus(academicStatus)
                .currentCompletedSemester(currentCompletedSemester)
                .graduationYear(graduationYear)
                .graduationType(graduationType)
                .phoneNumber(phoneNumber)
                .build();
    }

    public void update(
            String email,
            String name,
            String studentId,
            Integer admissionYear,
            UuidFile uuidFile,
            String nickname,
            String major,
            AcademicStatus academicStatus,
            Integer currentCompletedSemester,
            Integer graduationYear,
            GraduationType graduationType,
            String phoneNumber
    ) {
        this.email = email;
        this.name = name;
        this.studentId = studentId;
        this.admissionYear = admissionYear;
        this.uuidFile = uuidFile;
        this.nickname = nickname;
        this.major = major;
        this.academicStatus = academicStatus;
        this.currentCompletedSemester = currentCompletedSemester;
        this.graduationYear = graduationYear;
        this.graduationType = graduationType;
        this.phoneNumber = phoneNumber;
    }

    public String updatePassword(String newPassword) {
        this.password = newPassword;
        return this.password;
    }
}
