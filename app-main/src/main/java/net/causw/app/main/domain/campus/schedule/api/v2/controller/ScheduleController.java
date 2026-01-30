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

import net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.request.ScheduleRequest;
import net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.response.ScheduleResponse;
import net.causw.app.main.domain.campus.schedule.api.v2.controller.mapper.ScheduleDtoMapper;
import net.causw.app.main.domain.campus.schedule.service.v2.ScheduleService;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/schedules")
public class ScheduleController {

	private final ScheduleService scheduleService;
	private final ScheduleDtoMapper scheduleDtoMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
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

	@PutMapping("/{scheduleId}")
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	public ApiResponse<ScheduleResponse> updateSchedule(
		@RequestBody @Valid ScheduleRequest scheduleRequest,
		@PathVariable String scheduleId) {
		ScheduleDto scheduleDto = scheduleDtoMapper.toScheduleDto(scheduleRequest);

		ScheduleDto result = scheduleService.update(scheduleId, scheduleDto);

		return ApiResponse.success(scheduleDtoMapper.toScheduleResponse(result));
	}

	@DeleteMapping("/{scheduleId}")
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteSchedule(@PathVariable String scheduleId) {
		scheduleService.delete(scheduleId);
		return ApiResponse.success();
	}
}
