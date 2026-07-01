package net.causw.app.main.domain.community.report.api.v2.mapper;

import org.mapstruct.Mapper;

import net.causw.app.main.domain.community.report.api.v2.dto.request.ReportedUserListRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedCommentSummaryResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedPostSummaryResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedUserSummaryResponse;
import net.causw.app.main.domain.community.report.service.dto.ReportedCommentSummaryResult;
import net.causw.app.main.domain.community.report.service.dto.ReportedPostSummaryResult;
import net.causw.app.main.domain.community.report.service.dto.ReportedUserListCondition;
import net.causw.app.main.domain.community.report.service.dto.ReportedUserSummaryResult;

@Mapper(componentModel = "spring")
public interface ReportAdminMapper {

	ReportedUserListCondition toCondition(ReportedUserListRequest request);

	ReportedUserSummaryResponse toResponse(ReportedUserSummaryResult result);

	ReportedPostSummaryResponse toResponse(ReportedPostSummaryResult result);

	ReportedCommentSummaryResponse toResponse(ReportedCommentSummaryResult result);
}
