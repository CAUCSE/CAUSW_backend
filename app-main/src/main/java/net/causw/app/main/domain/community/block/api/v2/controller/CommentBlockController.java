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

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "댓글 작성자 차단", description = "댓글 ID를 기반으로 작성자를 서버에서 직접 조회하여 차단합니다. 익명 댓글의 경우 응답에서 신원 정보를 반환하지 않습니다.")
	public ApiResponse<BlockResponseDto> createBlock(
		@PathVariable String commentId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		BlockCreateResult result = commentBlockService.createBlock(
			commentBlockDtoMapper.toCommand(commentId, userDetails.getUser()));
		return ApiResponse.success(commentBlockDtoMapper.toResponse(result));
	}
}
