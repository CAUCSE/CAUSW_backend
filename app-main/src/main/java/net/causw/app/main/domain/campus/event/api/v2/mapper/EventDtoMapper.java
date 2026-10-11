package net.causw.app.main.domain.campus.event.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.campus.event.api.v2.dto.request.EventCreateRequest;
import net.causw.app.main.domain.campus.event.api.v2.dto.request.EventUpdateRequest;
import net.causw.app.main.domain.campus.event.api.v2.dto.response.EventResponse;
import net.causw.app.main.domain.campus.event.service.dto.EventCreateCommand;
import net.causw.app.main.domain.campus.event.service.dto.EventResult;
import net.causw.app.main.domain.campus.event.service.dto.EventUpdateCommand;

@Mapper(componentModel = "spring")
public interface EventDtoMapper {

	EventCreateCommand toCreateCommand(EventCreateRequest request);

	EventUpdateCommand toUpdateCommand(EventUpdateRequest request);

	EventResponse toResponse(EventResult result);

	List<EventResponse> toResponseList(List<EventResult> results);
}
