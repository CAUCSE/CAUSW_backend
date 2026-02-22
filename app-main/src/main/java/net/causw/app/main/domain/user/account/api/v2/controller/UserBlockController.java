package net.causw.app.main.domain.user.account.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.account.api.v2.dto.response.BlockResponseDto;
import net.causw.app.main.domain.user.account.api.v2.mapper.BlockDtoMapper;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.domain.user.relation.service.v2.BlockService;
import net.causw.app.main.domain.user.relation.service.v2.dto.BlockCreateResult;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts/{postId}")
@Tag(name = "차단 API (V2)", description = "유저 차단 관련 API")
public class UserBlockController {

	private final BlockService blockService;
	private final BlockDtoMapper blockDtoMapper;

	@PostMapping("/block")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "게시글 작성자 차단", description = "게시글을 통해 작성자를 차단합니다. 익명 게시글의 경우 응답에서 신원 정보를 반환하지 않습니다.")
	public ApiResponse<BlockResponseDto> createBlock(
		@PathVariable String postId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		BlockCreateResult result = blockService.createBlock(
			blockDtoMapper.toCommand(postId, userDetails.getUser()));
		return ApiResponse.success(blockDtoMapper.toResponse(result));
	}
}
