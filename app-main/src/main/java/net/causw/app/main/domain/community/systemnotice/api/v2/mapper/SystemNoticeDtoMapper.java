package net.causw.app.main.domain.community.systemnotice.api.v2.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.systemnotice.api.v2.dto.request.SystemNoticeCreateRequest;
import net.causw.app.main.domain.community.systemnotice.api.v2.dto.request.SystemNoticeUpdateRequest;
import net.causw.app.main.domain.community.systemnotice.api.v2.dto.response.SystemNoticeCreateResponse;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateCommand;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeCreateResult;
import net.causw.app.main.domain.community.systemnotice.service.dto.SystemNoticeUpdateCommand;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface SystemNoticeDtoMapper {

	@Mapping(target = "writer", source = "user")
	SystemNoticeCreateCommand toCreateCommand(SystemNoticeCreateRequest request, User user);

	@Mapping(target = "updater", source = "user")
	SystemNoticeUpdateCommand toUpdateCommand(SystemNoticeUpdateRequest request, User user);

	SystemNoticeCreateResponse toCreateResponse(SystemNoticeCreateResult result);

	List<SystemNoticeCreateResponse> toCreateResponseList(List<SystemNoticeCreateResult> results);
}
