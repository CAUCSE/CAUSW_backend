package net.causw.app.main.domain.campus.schedule.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.campus.schedule.api.v2.dto.request.CalendarFilterPreferenceRequest;
import net.causw.app.main.domain.campus.schedule.api.v2.dto.response.CalendarFilterPreferenceResponse;
import net.causw.app.main.domain.campus.schedule.service.CalendarFilterPreferenceService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users/me/calendar-filter")
@Tag(name = "Calendar Filter Public v2", description = "사용자별 캘린더 필터 설정 API")
public class CalendarFilterPreferenceController {

	private final CalendarFilterPreferenceService calendarFilterPreferenceService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "캘린더 필터 조회", description = "현재 로그인한 사용자가 저장한 캘린더 카테고리 필터를 조회합니다.")
	public ApiResponse<CalendarFilterPreferenceResponse> getCalendarFilter(
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(
			CalendarFilterPreferenceResponse.from(
				calendarFilterPreferenceService.getCategories(userDetails.getUserId())));
	}

	@PatchMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "캘린더 필터 저장", description = "현재 로그인한 사용자의 캘린더 카테고리 필터를 저장합니다.")
	public ApiResponse<CalendarFilterPreferenceResponse> updateCalendarFilter(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody CalendarFilterPreferenceRequest request) {

		return ApiResponse.success(
			CalendarFilterPreferenceResponse.from(
				calendarFilterPreferenceService.updateCategories(
					userDetails.getUserId(),
					request.categories())));
	}
}