package net.causw.app.main.domain.community.report.api.v2.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.report.api.v2.dto.request.ReportedUserListRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedCommentSummaryResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedPostSummaryResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ReportedUserSummaryResponse;
import net.causw.app.main.domain.community.report.api.v2.mapper.ReportAdminMapper;
import net.causw.app.main.domain.community.report.service.v2.ReportAdminService;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/reports")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "Report Admin v2", description = "관리자 신고 관리 API")
public class ReportAdminController {

	private final ReportAdminService reportAdminService;
	private final ReportAdminMapper reportAdminMapper;

	@Operation(summary = "신고된 회원 목록 조회", description = "신고된 회원을 이름/학번, 회원 상태, 학적 상태로 필터링하여 조회합니다.")
	@GetMapping("/users")
	public ApiResponse<PageResponse<ReportedUserSummaryResponse>> getReportedUserList(
		@ParameterObject ReportedUserListRequest request,
		@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
		return ApiResponse.success(
			PageResponse.from(
				reportAdminService.getReportedUserList(reportAdminMapper.toCondition(request), pageable)
					.map(reportAdminMapper::toResponse)));
	}

	@Operation(summary = "특정 회원의 신고된 게시글 조회", description = "특정 회원이 작성한 신고된 게시글 목록을 조회합니다.")
	@GetMapping("/users/{userId}/posts")
	public ApiResponse<PageResponse<ReportedPostSummaryResponse>> getReportedPostListByUser(
		@Parameter(description = "회원 ID") @PathVariable String userId,
		@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
		return ApiResponse.success(
			PageResponse.from(
				reportAdminService.getReportedPostListByUser(userId, pageable)
					.map(reportAdminMapper::toResponse)));
	}

	@Operation(summary = "특정 회원의 신고된 댓글 조회", description = "특정 회원이 작성한 신고된 댓글/대댓글 목록을 조회합니다.")
	@GetMapping("/users/{userId}/comments")
	public ApiResponse<PageResponse<ReportedCommentSummaryResponse>> getReportedCommentListByUser(
		@Parameter(description = "회원 ID") @PathVariable String userId,
		@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
		return ApiResponse.success(
			PageResponse.from(
				reportAdminService.getReportedCommentListByUser(userId, pageable)
					.map(reportAdminMapper::toResponse)));
	}
}
