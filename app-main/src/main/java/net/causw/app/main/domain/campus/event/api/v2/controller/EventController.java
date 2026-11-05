package net.causw.app.main.domain.campus.event.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.campus.event.api.v2.dto.response.EventResponse;
import net.causw.app.main.domain.campus.event.api.v2.mapper.EventDtoMapper;
import net.causw.app.main.domain.campus.event.service.EventService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/events")
@Tag(name = "Event Public v2", description = "홈 화면 이벤트 API")
public class EventController {

	private final EventService eventService;
	private final EventDtoMapper eventDtoMapper;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "홈 화면 이벤트 목록 조회", description = "노출이 활성화된 이벤트를 노출 순서대로 조회합니다. 빈 배열이면 이벤트가 없는 상태입니다.")
	public ApiResponse<List<EventResponse>> getActiveEvents() {
		return ApiResponse.success(eventDtoMapper.toResponseList(eventService.getActiveEvents()));
	}
}
