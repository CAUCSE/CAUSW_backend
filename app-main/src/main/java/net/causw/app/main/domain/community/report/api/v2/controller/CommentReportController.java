package net.causw.app.main.domain.community.report.api.v2.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.report.api.v2.dto.request.CommentReportCreateRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.response.PostReportReasonResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.CommentReportResponse;
import net.causw.app.main.domain.community.report.api.v2.mapper.CommentReportDtoMapper;
import net.causw.app.main.domain.community.report.enums.ReportReason;
import net.causw.app.main.domain.community.report.service.v2.CommentReportService;
import net.causw.app.main.domain.community.report.service.v2.dto.CommentReportCreateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/comments/{commentId}/reports")
@Tag(name = "댓글 신고 API (V2)", description = "댓글 신고 관련 API")
public class CommentReportController {

	private final CommentReportService commentReportService;
	private final CommentReportDtoMapper commentReportDtoMapper;

	@GetMapping("/reasons")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신고 사유 목록 조회", description = "신고 사유 선택 화면에 표시할 사유 목록을 반환합니다.")
	public ApiResponse<List<PostReportReasonResponse>> getReportReasons(@PathVariable String commentId) {
		List<PostReportReasonResponse> reasons = Arrays.stream(ReportReason.values())
			.map(PostReportReasonResponse::from)
			.toList();
		return ApiResponse.success(reasons);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "댓글 신고", description = "댓글을 신고합니다. 본인 댓글 신고 및 중복 신고는 불가합니다.")
	public ApiResponse<CommentReportResponse> createReport(
		@PathVariable String commentId,
		@Valid @RequestBody CommentReportCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		CommentReportCreateResult result = commentReportService.createReport(
			commentReportDtoMapper.toCommand(request, commentId, userDetails.getUser()));
		return ApiResponse.success(commentReportDtoMapper.toResponse(result));
	}
}
