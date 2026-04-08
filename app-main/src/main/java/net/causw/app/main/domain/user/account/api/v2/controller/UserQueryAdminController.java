package net.causw.app.main.domain.user.account.api.v2.controller;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.api.v2.dto.request.UserSearchCondition;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDailyCountResponse;
import net.causw.app.main.domain.user.account.api.v2.dto.response.UserSearchListResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserSearchConditionMapper;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserSearchListMapper;
import net.causw.app.main.domain.user.account.service.UserQueryService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/users")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "User Admin Query v2", description = "게시판 관리 등 관리자 기능에서 공통으로 사용되는 사용자 검색용 API")
public class UserQueryAdminController {

	private final UserQueryService userQueryService;
	private final UserSearchConditionMapper userSearchConditionMapper;
	private final UserSearchListMapper userSearchListMapper;

	@GetMapping("/search")
	@Operation(summary = "유저 검색", description = "유저를 검색한다.")
	public ApiResponse<UserSearchListResponse> searchUsers(
		@ModelAttribute UserSearchCondition userSearchCondition) {
		var result = userQueryService.searchUsers(
			userSearchConditionMapper.toServiceDto(userSearchCondition));
		UserSearchListResponse response = userSearchListMapper.toResponse(result);

		return ApiResponse.success(response);
	}

	@Operation(summary = "일일 신규 가입자 수 조회")
	@GetMapping("/daily-count")
	public ApiResponse<UserDailyCountResponse> getDailySignupCount(
		@RequestParam(value = "targetDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
		LocalDate date = (targetDate != null) ? targetDate : LocalDate.now(ZoneId.of("Asia/Seoul"));
		Long count = userQueryService.getDailySignupStats(date);
		return ApiResponse.success(new UserDailyCountResponse(date, count));
	}
}
