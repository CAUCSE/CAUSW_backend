package net.causw.app.main.domain.user.account.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

@Schema(description = "관리자 유저 목록 조회 요청")
public record UserListRequest(

        @Schema(description = "이름 또는 학번 검색 키워드", example = "홍길동")
        String keyword,

        @Schema(description = "유저 상태 필터링", example = "ACTIVE")
        UserState state,

        @Schema(description = "학적 상태 필터링", example = "ENROLLED")
        AcademicStatus academicStatus,

        @Schema(description = "소속 학과 필터링", example = "SCHOOL_OF_SW")
        Department department,

        @Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0")
        @Min(0)
        Integer page,

        @Schema(description = "페이지 크기", example = "10", minimum = "1", maximum = "100")
        @Min(1) @Max(100)
        Integer size
) {}
