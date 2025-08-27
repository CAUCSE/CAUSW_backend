package net.causw.app.main.controller;

import static org.springframework.util.MimeTypeUtils.*;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.dto.userBlock.response.CreateBlockByChildCommentResponseDto;
import net.causw.app.main.dto.userBlock.response.CreateBlockByCommentResponseDto;
import net.causw.app.main.dto.userBlock.response.CreateBlockByPostResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.userBlock.useCase.BlockByChildCommentUseCaseService;
import net.causw.app.main.service.userBlock.useCase.BlockByCommentUseCaseService;
import net.causw.app.main.service.userBlock.useCase.BlockByPostUseCaseService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/blocks", produces = APPLICATION_JSON_VALUE)
public class UserBlockController {

	private final BlockByPostUseCaseService blockByPostUseCaseService;
	private final BlockByCommentUseCaseService blockByCommentUseCaseService;
	private final BlockByChildCommentUseCaseService blockByChildCommentUseCaseService;

	@PostMapping("/by-post/{postId}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "게시물을 통한 차단 api", description = "게시물을 통해 유저를 차단할 수 있습니다.")
	public CreateBlockByPostResponseDto createBlockByPost(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("postId") String postId

	) {
		return blockByPostUseCaseService.execute(userDetails, postId);
	}

	@PostMapping("/by-comment/{commentId}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "댓글을 통한 차단 api", description = "댓글을 통해 유저를 차단할 수 있습니다.")
	public CreateBlockByCommentResponseDto createBlockByComment(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("commentId") String commentId

	) {
		return blockByCommentUseCaseService.execute(userDetails, commentId);
	}

	@PostMapping("/by-child-comment/{childCommentId}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "대댓글을 통한 차단 api", description = "대댓글을 통해 유저를 차단할 수 있습니다.")
	public CreateBlockByChildCommentResponseDto createBlockByChildComment(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("childCommentId") String childCommentId

	) {
		return blockByChildCommentUseCaseService.execute(userDetails, childCommentId);
	}
}
