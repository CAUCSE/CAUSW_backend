package net.causw.application.dto.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequestDto {

    @Schema(description = "이메일", example = "yebin@cau.ac.kr", required = true)
    private String email;

    @Schema(description = "이름", example = "이예빈", required = true)
    private String name;

    @Schema(description = "비밀번호", example = "password00!!", required = true)
    private String password;

    @Schema(description = "학번", example = "20209999", required = true)
    private String studentId;

    @Schema(description = "입학년도", example = "2020", required = true)
    private Integer admissionYear;

    @Schema(description = "프로필 이미지 URL", example = "", required = true)
    private String profileImage;

    @Schema(description = "학부생 인증 이미지", example = "", required = true)
    private List<String> attachImages;

    // 새로 추가된 필드들
    @Schema(description = "닉네임", example = "푸앙", required = true)
    private String nickname;

    @Schema(description = "학부/학과", example = "소프트웨어학부", required = true)
    private String major;

    @Schema(description = "학적상태", example = "ENROLLED", required = true)
    private AcademicStatus academicStatus;

    @Schema(description = "현재 등록 완료된 학기", example = "6(3학년 2학기)", required = true)
    private Integer currentCompletedSemester;

    @Schema(description = "졸업시기 년", example = "2026", required = true)
    private Integer graduationYear;

    @Schema(description = "졸업시기 월", example = "2", required = true)
    private Integer graduationMonth;

    @Schema(description = "전화번호", example = "01012345678", required = true)
    private String phoneNumber;

    public User toEntity(String encodedPassword, Set<Role> roles, UserState state) {
        return User.builder()
                .email(email)
                .name(name)
                .roles(roles)
                .state(state)
                .password(encodedPassword)
                .studentId(studentId)
                .admissionYear(admissionYear)
                .attachImages(attachImages)
                .profileImage(profileImage)
                .nickname(nickname)
                .major(major)
                .academicStatus(academicStatus)
                .currentCompletedSemester(currentCompletedSemester)
                .graduationYear(graduationYear)
                .graduationMonth(graduationMonth)
                .phoneNumber(phoneNumber)
                .build();
    }
}
