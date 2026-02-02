package net.causw.app.main.domain.user.account.api.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.GraduationType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

@Schema(description = "관리자 사용자 상세 정보 응답")
public record UserDetailResponse(

        @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "이메일", example = "yebin@cau.ac.kr")
        String email,

        @Schema(description = "이름", example = "이예빈")
        String name,

        @Schema(description = "학번", example = "20209999")
        String studentId,

        @Schema(description = "입학년도", example = "2020")
        Integer admissionYear,

        @Schema(description = "사용자 권한 목록", example = "[\"COMMON\"]")
        List<String> roles,

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.causw.net/profile/default.png", nullable = true)
        String profileImageUrl,

        @Schema(description = "사용자 상태", example = "AWAIT")
        UserState state,

        @Schema(description = "닉네임", example = "푸앙")
        String nickname,

        @Schema(description = "전공", example = "소프트웨어학부")
        String major,

        @Schema(description = "소속 학부", example = "SCHOOL_OF_SW")
        Department department,

        @Schema(description = "학적 상태", example = "ENROLLED")
        AcademicStatus academicStatus,

        @Schema(description = "현재 이수 학기 수", example = "6")
        Integer currentCompletedSemester,

        @Schema(description = "졸업 예정 연도", example = "2026")
        Integer graduationYear,

        @Schema(description = "졸업 유형", example = "FEBRUARY")
        GraduationType graduationType,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phoneNumber,

        @Schema(description = "거절 또는 탈퇴 사유", example = "재학 증빙 서류 미제출", nullable = true
        )
        String rejectionOrDropReason,

        @Schema(description = "계정 생성일시", example = "2024-03-24T10:15:30")
        LocalDateTime createdAt,

        @Schema(description = "계정 정보 수정일시", example = "2024-08-24T18:42:10")
        LocalDateTime updatedAt
) {}
