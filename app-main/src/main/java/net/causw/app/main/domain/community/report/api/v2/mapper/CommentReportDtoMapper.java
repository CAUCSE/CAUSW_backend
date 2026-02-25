package net.causw.app.main.domain.community.report.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.report.api.v2.dto.request.CommentReportCreateRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.response.CommentReportResponse;
import net.causw.app.main.domain.community.report.service.v2.dto.CommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.CommentReportCreateResult;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface CommentReportDtoMapper {

	@Mapping(target = "reporter", source = "user")
	@Mapping(target = "commentId", source = "commentId")
	CommentReportCreateCommand toCommand(CommentReportCreateRequest request, String commentId, User user);

	CommentReportResponse toResponse(CommentReportCreateResult result);
}
