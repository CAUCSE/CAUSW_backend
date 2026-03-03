package net.causw.app.main.domain.community.block.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.block.api.v2.mapper.CommentBlockDtoMapper;
import net.causw.app.main.domain.community.block.service.CommentBlockService;
import net.causw.app.main.domain.user.account.api.v2.dto.response.BlockResponseDto;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/comments/{commentId}/blocks")
@Tag(name = "차단 API (V2)", description = "댓글 기반 유저 차단 API")
public class CommentBlockController {

	private final CommentBlockService commentBlockService;
	private final CommentBlockDtoMapper commentBlockDtoMapper;

	@PostMapping("/{targetUserId}")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "댓글 기반 유저 차단", description = "댓글에서 특정 유저를 차단합니다. 본인 차단 및 중복 차단은 불가합니다.")
	public ApiResponse<BlockResponseDto> createBlock(
		@PathVariable String commentId,
		@PathVariable String targetUserId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		BlockCreateResult result = commentBlockService.createBlock(
			commentBlockDtoMapper.toCommand(targetUserId, commentId, userDetails.getUser()));
		return ApiResponse.success(commentBlockDtoMapper.toResponse(result));
	}
}
