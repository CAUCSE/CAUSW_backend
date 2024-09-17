package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.GraduationType;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserResponseDto {

    @Schema(description = "고유 id값", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @Schema(description = "이름", example = "이예빈")
    private String name;

    @Schema(description = "학번", example = "20209999")
    private String studentId;

    @Schema(description = "입학년도", example = "2020")
    private Integer admissionYear;

    @Schema(description = "역할", example = "COMMON")
    private Set<Role> roles;

    @Schema(description = "프로필 이미지 URL", example = "")
    private String profileImageUrl;

    @Schema(description = "상태", example = "AWAIT")
    private UserState state;

    @Schema(description = "리더일 경우, 동아리 고유 id값 리스트", example = "['uuid 형식의 String 값입니다.', ...]")
    private List<String> circleIdIfLeader;

    @Schema(description = "리더일 경우, 동아리 이름 리스트", example = "[개발 동아리, 퍼주마,..]")
    private List<String> circleNameIfLeader;

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

    @Schema(description = "가입 거부 OR DROP 이유", example = "부적절한 행위로 DROP")
    private String rejectionOrDropReason;

    @Schema(description = "생성일자", example = "2024-03-24")
    private LocalDateTime createdAt;

    @Schema(description = "수정일자", example = "2024-08-24")
    private LocalDateTime updatedAt;

}
