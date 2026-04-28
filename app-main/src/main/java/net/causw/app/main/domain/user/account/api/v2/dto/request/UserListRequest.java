package net.causw.app.main.domain.user.account.api.v2.dto.request;

import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserSortType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 유저 목록 조회 요청")
public record UserListRequest(

	@Schema(description = "이름, 학번 또는 이메일 검색 키워드", example = "홍길동") String keyword,

	@Schema(description = "유저 상태 멀티 필터링 (미지정 시 ACTIVE)", example = "[\"ACTIVE\"]") List<UserState> states,

	@Schema(description = "학적 상태 필터링", example = "ENROLLED") AcademicStatus academicStatus,

	@Schema(description = "소속 학과 필터링", example = "SCHOOL_OF_SW") Department department,

	@Schema(description = "입학년도 시작 (포함)", example = "2020") Integer admissionYearFrom,

	@Schema(description = "입학년도 끝 (포함)", example = "2023") Integer admissionYearTo,

	@Schema(description = "정렬 기준 (기본: CREATED_AT_DESC)", example = "CREATED_AT_DESC") UserSortType sortBy) {
}
