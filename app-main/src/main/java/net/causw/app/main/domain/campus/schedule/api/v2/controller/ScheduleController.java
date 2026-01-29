package net.causw.app.main.domain.campus.schedule.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.request.ScheduleCreateRequest;
import net.causw.app.main.domain.campus.schedule.api.v2.controller.dto.response.ScheduleResponse;
import net.causw.app.main.domain.campus.schedule.api.v2.controller.mapper.ScheduleDtoMapper;
import net.causw.app.main.domain.campus.schedule.service.v2.ScheduleService;
import net.causw.app.main.domain.campus.schedule.service.v2.dto.ScheduleDto;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;

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
	public ScheduleResponse createSchedule(
		@RequestBody @Valid ScheduleCreateRequest scheduleCreateRequest,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		// api dto -> application dto
		ScheduleDto scheduleDto = scheduleDtoMapper.toScheduleDto(
			scheduleCreateRequest,
			customUserDetails.getUser());

		ScheduleDto result = scheduleService.save(scheduleDto);

		//application dto -> api dto
		return scheduleDtoMapper.toScheduleResponse(result);
	}
}
