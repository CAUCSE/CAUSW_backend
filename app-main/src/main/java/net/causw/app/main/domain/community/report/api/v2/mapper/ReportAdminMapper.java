package net.causw.app.main.domain.community.report.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.report.api.v2.dto.request.ReportedUserListRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedCommentSummaryResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedUserSummaryResponse;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedCommentSummaryResult;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedUserListCondition;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedUserSummaryResult;

@Mapper(componentModel = "spring")
public interface ReportAdminMapper {

	ReportedUserListCondition toCondition(ReportedUserListRequest request);

	ReportedUserSummaryResponse toResponse(ReportedUserSummaryResult result);

	ReportedCommentSummaryResponse toResponse(ReportedCommentSummaryResult result);
}
