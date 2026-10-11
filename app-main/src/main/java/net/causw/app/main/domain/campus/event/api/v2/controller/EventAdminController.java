package net.causw.app.main.domain.campus.event.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.core.aop.annotation.RequireAdminRole;
import net.causw.app.main.core.aop.enums.AdminTarget;
import net.causw.app.main.domain.campus.event.api.v2.dto.request.EventCreateRequest;
import net.causw.app.main.domain.campus.event.api.v2.dto.request.EventUpdateRequest;
import net.causw.app.main.domain.campus.event.api.v2.dto.response.EventResponse;
import net.causw.app.main.domain.campus.event.api.v2.mapper.EventDtoMapper;
import net.causw.app.main.domain.campus.event.service.EventService;
import net.causw.app.main.domain.campus.event.service.dto.EventResult;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/events")
@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
@RequireAdminRole(target = AdminTarget.ALL_ADMIN)
@Tag(name = "Event Admin v2", description = "관리자 홈 화면 이벤트 관리 API")
public class EventAdminController {

	private final EventService eventService;
	private final EventDtoMapper eventDtoMapper;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "이벤트 전체 조회", description = "노출 여부와 관계없이 삭제되지 않은 모든 이벤트를 노출 순서대로 조회합니다.")
	public ApiResponse<List<EventResponse>> getAllEvents() {
		return ApiResponse.success(eventDtoMapper.toResponseList(eventService.getAllEvents()));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "이벤트 등록", description = "배너 이미지는 Presigned URL(FilePath.EVENT)로 먼저 업로드한 뒤 UUID 를 전달합니다.")
	public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
		EventResult result = eventService.createEvent(eventDtoMapper.toCreateCommand(request));
		return ApiResponse.success(eventDtoMapper.toResponse(result));
	}

	@PutMapping("/{eventId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "이벤트 수정", description = "이벤트 내용과 노출 활성화 여부를 수정합니다. 이미지 UUID 가 바뀌면 기존 이미지는 삭제됩니다.")
	public ApiResponse<EventResponse> updateEvent(
		@PathVariable("eventId") String eventId,
		@Valid @RequestBody EventUpdateRequest request) {
		EventResult result = eventService.updateEvent(eventId, eventDtoMapper.toUpdateCommand(request));
		return ApiResponse.success(eventDtoMapper.toResponse(result));
	}

	@DeleteMapping("/{eventId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "이벤트 삭제", description = "이벤트를 삭제합니다.")
	public ApiResponse<Void> deleteEvent(@PathVariable("eventId") String eventId) {
		eventService.deleteEvent(eventId);
		return ApiResponse.success();
	}
}
