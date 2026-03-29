package net.causw.app.main.domain.user.account.api.v2.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.api.v2.dto.response.UserDailyCountResponse;
import net.causw.app.main.domain.user.account.api.v2.mapper.UserStaticsMapper;
import net.causw.app.main.domain.user.account.service.UserAdminService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/users")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "User Admin v2", description = "관리자용 사용자 관련 집계 및 통계 API")
public class UserStaticsAdminController {

	private final UserAdminService userAdminService;
	private final UserStaticsMapper userStaticsMapper;

	@Operation(summary = "일일 신규 가입자 수 조회")
	@GetMapping("/daily-count")
	public ApiResponse<UserDailyCountResponse> getDailySignupCount(
		@RequestParam(value = "targetDate", required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate
	) {
		LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();
		var result = userAdminService.getDailySignupStats(date);
		return ApiResponse.success(userStaticsMapper.toDailyCountResponse(result));
	}
}
