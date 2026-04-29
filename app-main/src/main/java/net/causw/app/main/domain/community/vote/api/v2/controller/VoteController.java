package net.causw.app.main.domain.community.vote.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.vote.api.v2.dto.request.CreateVoteRequest;
import net.causw.app.main.domain.community.vote.api.v2.dto.response.VoteResponse;
import net.causw.app.main.domain.community.vote.service.v2.VoteService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Vote Public v2", description = "투표 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/votes")
public class VoteController {

	private final VoteService voteV2Service;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "투표 생성", description = "게시글에 투표를 생성합니다.")
	public ApiResponse<VoteResponse> createVote(
		@Valid @RequestBody CreateVoteRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(voteV2Service.createVote(request, userDetails.getUser()));
	}

	@PostMapping("/{voteId}/options/{optionId}/toggle")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "투표 옵션 토글",
		description = "같은 옵션 재클릭 시 취소, 새 옵션 클릭 시 추가. allowMultiple=false이면 기존 선택이 자동 해제됩니다.")
	public ApiResponse<VoteResponse> toggleVote(
		@PathVariable String voteId,
		@PathVariable String optionId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(voteV2Service.toggleVote(voteId, optionId, userDetails.getUser()));
	}

	@GetMapping("/{voteId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "투표 조회", description = "투표 ID로 투표 정보와 내가 선택한 옵션을 조회합니다.")
	public ApiResponse<VoteResponse> findVoteById(
		@PathVariable String voteId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(voteV2Service.findVoteById(voteId, userDetails.getUser()));
	}

	@GetMapping("/post/{postId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "게시글 ID로 투표 조회", description = "게시글 ID로 해당 게시글의 투표를 조회합니다.")
	public ApiResponse<VoteResponse> findVoteByPostId(
		@PathVariable String postId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(voteV2Service.findVoteByPostId(postId, userDetails.getUser()));
	}

	@PostMapping("/{voteId}/end")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "투표 종료", description = "투표를 종료합니다. 게시글 작성자만 가능합니다.")
	public ApiResponse<VoteResponse> endVote(
		@PathVariable String voteId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(voteV2Service.endVote(voteId, userDetails.getUser()));
	}

	@PostMapping("/{voteId}/restart")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "투표 재시작", description = "종료된 투표를 재시작합니다. 게시글 작성자만 가능합니다.")
	public ApiResponse<VoteResponse> restartVote(
		@PathVariable String voteId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(voteV2Service.restartVote(voteId, userDetails.getUser()));
	}
}
