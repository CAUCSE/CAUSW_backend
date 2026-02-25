package net.causw.app.main.domain.community.report.api.v2.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import net.causw.app.main.domain.community.report.api.v2.dto.request.ChildCommentReportCreateRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ChildCommentReportResponse;
import net.causw.app.main.domain.community.report.service.v2.dto.ChildCommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.ChildCommentReportCreateResult;
import net.causw.app.main.domain.user.account.entity.user.User;

@Mapper(componentModel = "spring")
public interface ChildCommentReportDtoMapper {

	@Mapping(target = "reporter", source = "user")
	@Mapping(target = "childCommentId", source = "childCommentId")
	ChildCommentReportCreateCommand toCommand(
		ChildCommentReportCreateRequest request, String childCommentId, User user);

	ChildCommentReportResponse toResponse(ChildCommentReportCreateResult result);
}
