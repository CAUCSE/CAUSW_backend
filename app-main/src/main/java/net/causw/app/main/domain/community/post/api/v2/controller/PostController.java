package net.causw.app.main.domain.community.post.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.post.service.PostService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.UnauthorizedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
public class PostController {

	private final PostService postService;

	@PostMapping(value = "/{id}/like")
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "게시글 좋아요 저장 API(완료)", description = "특정 유저가 특정 게시글에 좋아요를 누른 걸 저장하는 Api 입니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
		@ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
		@ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
		@ApiResponse(responseCode = "4001", description = "좋아요를 이미 누른 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
		@ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
	})
	public void likePost(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		this.postService.likePost(userDetails.getUser().getId(), id);
	}

	@DeleteMapping(value = "/{id}/like")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "게시글 좋아요 취소 API(완료)", description = "특정 유저가 특정 게시글에 좋아요를 누른 걸 취소하는 Api 입니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
		@ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
		@ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
		@ApiResponse(responseCode = "4000", description = "좋아요를 누르지 않은 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
		@ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
		@ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
	})
	public void cancelLikePost(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		this.postService.cancelLikePost(userDetails.getUser().getId(), id);
	}

}
