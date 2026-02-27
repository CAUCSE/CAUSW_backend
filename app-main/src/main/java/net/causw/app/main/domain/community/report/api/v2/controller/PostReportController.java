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

import net.causw.app.main.domain.community.report.api.v2.dto.request.PostReportCreateRequestDto;
import net.causw.app.main.domain.community.report.api.v2.dto.response.PostReportReasonResponseDto;
import net.causw.app.main.domain.community.report.api.v2.dto.response.PostReportResponseDto;
import net.causw.app.main.domain.community.report.api.v2.mapper.PostReportDtoMapper;
import net.causw.app.main.domain.community.report.enums.ReportReason;
import net.causw.app.main.domain.community.report.service.v2.PostReportService;
import net.causw.app.main.domain.community.report.service.v2.dto.PostReportCreateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts/{postId}/reports")
@Tag(name = "게시글 신고 API (V2)", description = "게시글 신고 관련 API")
public class PostReportController {

	private final PostReportService postReportService;
	private final PostReportDtoMapper postReportDtoMapper;

	@GetMapping("/reasons")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신고 사유 목록 조회", description = "신고 사유 선택 화면에 표시할 사유 목록을 반환합니다.")
	public ApiResponse<List<PostReportReasonResponseDto>> getReportReasons() {
		List<PostReportReasonResponseDto> reasons = Arrays.stream(ReportReason.values())
			.map(PostReportReasonResponseDto::from)
			.toList();
		return ApiResponse.success(reasons);
	}

	//
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "게시글 신고", description = """
		신고 사유 선택 → 확인  → 이 API 호출 순서로 진행생각합니다。근데 확인에 관한 내용이없어 고민중
		- 본인 게시글 신고 불가 (400)
		- 동일 게시글 중복 신고 불가 (409)
		- 신고 접수 후 관리자가 검토하여 처리/반려하는 방향으로 생각중입니다
		""")
	public ApiResponse<PostReportResponseDto> createReport(
		@PathVariable String postId,
		@Valid @RequestBody PostReportCreateRequestDto request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		PostReportCreateResult result = postReportService.createReport(
			postReportDtoMapper.toCommand(request, postId, userDetails.getUser()));
		return ApiResponse.success(postReportDtoMapper.toResponse(result));
	}

}
