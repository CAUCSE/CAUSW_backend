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
import net.causw.app.main.domain.community.report.api.v2.dto.request.CommentReportCreateRequest;
import net.causw.app.main.domain.community.report.api.v2.dto.request.PostReportCreateRequestDto;
import net.causw.app.main.domain.community.report.api.v2.dto.response.ChildCommentReportResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.CommentReportResponse;
import net.causw.app.main.domain.community.report.api.v2.dto.response.PostReportResponseDto;
import net.causw.app.main.domain.community.report.api.v2.mapper.ChildCommentReportDtoMapper;
import net.causw.app.main.domain.community.report.api.v2.mapper.CommentReportDtoMapper;
import net.causw.app.main.domain.community.report.api.v2.mapper.PostReportDtoMapper;
import net.causw.app.main.domain.community.report.service.v2.ReportService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController("reportControllerV2")
@RequiredArgsConstructor
@RequestMapping("/api/v2")
@Tag(name = "Report Public v2", description = "게시글/댓글/대댓글 신고 관련 API")
public class ReportController {

	private final ReportService reportService;
	private final PostReportDtoMapper postReportDtoMapper;
	private final CommentReportDtoMapper commentReportDtoMapper;
	private final ChildCommentReportDtoMapper childCommentReportDtoMapper;

	@PostMapping("/posts/{postId}/reports")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "게시글 신고", description = """
		신고 사유 선택 → 확인  → 이 API 호출 순서로 진행생각합니다。근데 확인에 관한 내용이없어 고민중
		- 본인 게시글 신고 불가 (400)
		- 동일 게시글 중복 신고 불가 (409)
		- 신고 접수 후 관리자가 검토하여 처리/반려하는 방향으로 생각중입니다
		""")
	public ApiResponse<PostReportResponseDto> createPostReport(
		@PathVariable String postId,
		@Valid @RequestBody PostReportCreateRequestDto request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(postReportDtoMapper.toResponse(
			reportService.createPostReport(
				postReportDtoMapper.toCommand(request, postId, userDetails.getUser()))));
	}

	@PostMapping("/comments/{commentId}/reports")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "댓글 신고", description = "댓글을 신고합니다. 본인 댓글 신고 및 중복 신고는 불가합니다.")
	public ApiResponse<CommentReportResponse> createCommentReport(
		@PathVariable String commentId,
		@Valid @RequestBody CommentReportCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(commentReportDtoMapper.toResponse(
			reportService.createCommentReport(
				commentReportDtoMapper.toCommand(request, commentId, userDetails.getUser()))));
	}

	@PostMapping("/child-comments/{childCommentId}/reports")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "대댓글 신고", description = "대댓글을 신고합니다. 본인 대댓글 신고 및 중복 신고는 불가합니다.")
	public ApiResponse<ChildCommentReportResponse> createChildCommentReport(
		@PathVariable String childCommentId,
		@Valid @RequestBody ChildCommentReportCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(childCommentReportDtoMapper.toResponse(
			reportService.createChildCommentReport(
				childCommentReportDtoMapper.toCommand(request, childCommentId, userDetails.getUser()))));
	}
}
