package net.causw.app.main.domain.community.report.api.v2.mapper;

import net.causw.app.main.domain.community.report.api.v2.dto.request.PostReportCreateRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.report.api.v2.dto.request.PostReportCreateRequestDto;
import net.causw.app.main.domain.community.report.api.v2.dto.response.PostReportResponseDto;
import net.causw.app.main.domain.community.report.service.v2.dto.PostReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.PostReportCreateResult;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface PostReportDtoMapper {

	@Mapping(target = "reporter", source = "user")
	@Mapping(target = "postId", source = "postId")
	PostReportCreateCommand toCommand(PostReportCreateRequestDto request, String postId, User user);

	PostReportResponseDto toResponse(PostReportCreateResult result);
}
