package net.causw.app.main.domain.campus.schedule.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.campus.schedule.api.v2.dto.request.ScheduleRequest;
import net.causw.app.main.domain.campus.schedule.api.v2.dto.response.ScheduleResponse;
import net.causw.app.main.domain.campus.schedule.api.v2.mapper.ScheduleDtoMapper;
import net.causw.app.main.domain.campus.schedule.service.v2.ScheduleService;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Schedule Admin v2", description = "일정 관리자 전용 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/schedules")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
public class ScheduleAdminController {

	private final ScheduleService scheduleService;
	private final ScheduleDtoMapper scheduleDtoMapper;

	@Operation(summary = "일정 생성", description = "새로운 일정을 생성합니다. 관리자 권한이 필요합니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ScheduleResponse> createSchedule(
		@RequestBody @Valid ScheduleRequest scheduleRequest,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		// api dto -> application dto
		ScheduleDto scheduleDto = scheduleDtoMapper.toScheduleDto(
			scheduleRequest,
			customUserDetails.getUser());

		ScheduleDto result = scheduleService.save(scheduleDto);

		//application dto -> api dto
		return ApiResponse.success(scheduleDtoMapper.toScheduleResponse(result));
	}

	@Operation(summary = "일정 수정", description = "기존 일정을 수정합니다. 관리자 권한이 필요합니다.")
	@PutMapping("/{scheduleId}")
	public ApiResponse<ScheduleResponse> updateSchedule(
		@RequestBody @Valid ScheduleRequest scheduleRequest,
		@PathVariable String scheduleId) {
		ScheduleDto scheduleDto = scheduleDtoMapper.toScheduleDto(scheduleRequest);

		ScheduleDto result = scheduleService.update(scheduleId, scheduleDto);

		return ApiResponse.success(scheduleDtoMapper.toScheduleResponse(result));
	}

	@Operation(summary = "일정 삭제", description = "일정을 삭제합니다. 관리자 권한이 필요합니다.")
	@DeleteMapping("/{scheduleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteSchedule(@PathVariable String scheduleId) {
		scheduleService.delete(scheduleId);
		return ApiResponse.success();
	}

}
