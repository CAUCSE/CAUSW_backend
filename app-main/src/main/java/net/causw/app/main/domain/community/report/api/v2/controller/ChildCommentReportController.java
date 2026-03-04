package net.causw.app.main.domain.community.report.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.report.api.v2.dto.request.ChildCommentReportCreateRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ChildCommentReportResponse;
import net.causw.app.main.domain.community.report.api.v2.mapper.ChildCommentReportDtoMapper;
import net.causw.app.main.domain.community.report.service.v2.ChildCommentReportService;
import net.causw.app.main.domain.community.report.service.v2.dto.ChildCommentReportCreateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/child-comments/{childCommentId}/reports")
@Tag(name = "대댓글 신고 API (V2)", description = "대댓글 신고 관련 API")
public class ChildCommentReportController {

	private final ChildCommentReportService childCommentReportService;
	private final ChildCommentReportDtoMapper childCommentReportDtoMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "대댓글 신고", description = "대댓글을 신고합니다. 본인 대댓글 신고 및 중복 신고는 불가합니다.")
	public ApiResponse<ChildCommentReportResponse> createReport(
		@PathVariable String childCommentId,
		@Valid @RequestBody ChildCommentReportCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		ChildCommentReportCreateResult result = childCommentReportService.createReport(
			childCommentReportDtoMapper.toCommand(request, childCommentId, userDetails.getUser()));
		return ApiResponse.success(childCommentReportDtoMapper.toResponse(result));
	}
}
