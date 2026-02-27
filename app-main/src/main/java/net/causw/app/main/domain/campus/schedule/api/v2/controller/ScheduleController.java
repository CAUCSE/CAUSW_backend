package net.causw.app.main.domain.campus.schedule.api.v2.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.campus.schedule.api.v2.dto.response.ScheduleListResponse;
import net.causw.app.main.domain.campus.schedule.api.v2.dto.response.ScheduleResponse;
import net.causw.app.main.domain.campus.schedule.api.v2.mapper.ScheduleDtoMapper;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.ScheduleService;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "일정 API V2", description = "일정 조회 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/schedules")
public class ScheduleController {

	private final ScheduleService scheduleService;
	private final ScheduleDtoMapper scheduleDtoMapper;

	@Operation(summary = "일정 조회", description = "조건에 따라 일정을 조회합니다. <br>" +
		"from과 to가 지정되지 않으면 현재 월의 일정을 조회합니다. <br>" +
		"types로 일정 유형을 필터링할 수 있습니다. <br><br>" +
		"types 사용 예시: <br>" +
		"1) 쉼표 구분: ?types=ACADEMIC,CLUB,EXAM <br>" +
		"2) 반복: ?types=ACADEMIC&types=CLUB&types=EXAM <br>" +
		"3) 생략: 모든 타입 조회")
	@GetMapping
	public ApiResponse<ScheduleListResponse> readSchedules(
		@RequestParam(required = false) LocalDateTime from,
		@RequestParam(required = false) LocalDateTime to,
		@RequestParam(required = false) java.util.List<ScheduleType> types) {

		LocalDateTime startDate = from;
		LocalDateTime endDate = to;

		if (from == null || to == null) {
			startDate = LocalDateTime.now().toLocalDate().withDayOfMonth(1).atStartOfDay();
			endDate = LocalDateTime.now().toLocalDate().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusSeconds(1);
		}

		return ApiResponse.success(
			scheduleDtoMapper.toScheduleListResponse(
				scheduleService.findByCondition(startDate, endDate, types)));
	}

	@Operation(summary = "일정 단건 조회", description = "특정 ID의 일정을 조회합니다.")
	@GetMapping("/{scheduleId}")
	public ApiResponse<ScheduleResponse> readSchedule(@PathVariable String scheduleId) {
		ScheduleDto result = scheduleService.findById(scheduleId);
		return ApiResponse.success(scheduleDtoMapper.toScheduleResponse(result));
	}

}
