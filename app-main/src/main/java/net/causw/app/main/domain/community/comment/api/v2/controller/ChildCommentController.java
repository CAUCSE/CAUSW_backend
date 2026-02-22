package net.causw.app.main.domain.community.comment.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.ChildCommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.service.ChildCommentService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/child-comments")
public class ChildCommentController {
	private final ChildCommentService childCommentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "대댓글 생성 API", description = "대댓글을 생성하는 api입니다.")
	public ApiResponse<ChildCommentResponseDto> createChildComment(
		@Valid @RequestBody ChildCommentCreateRequestDto childCommentCreateRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(
			childCommentService.createChildComment(userDetails.getUser().getId(), childCommentCreateRequestDto));
	}

	@PutMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "대댓글 수정 API", description = "특정 대댓글을 수정하는 API입니다.")
	public ApiResponse<ChildCommentResponseDto> updateChildComment(
		@PathVariable("id") String id,
		@Valid @RequestBody ChildCommentUpdateRequestDto childCommentUpdateRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(
			childCommentService.updateChildComment(userDetails.getUser().getId(), id, childCommentUpdateRequestDto));
	}

	@DeleteMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "대댓글 삭제 API", description = "특정 대댓글을 삭제하는 API입니다.")
	public ApiResponse<ChildCommentResponseDto> deleteChildComment(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		return ApiResponse.success(childCommentService.deleteChildComment(userDetails.getUser().getId(), id));
	}

	@PostMapping(value = "/{id}/like")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "대댓글 좋아요 저장 API", description = "특정 유저가 특정 대댓글에 좋아요를 누른 걸 저장하는 Api 입니다.")
	public ApiResponse<Void> likeChildComment(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		childCommentService.likeChildComment(userDetails.getUser().getId(), id);
		return ApiResponse.success();
	}

	@DeleteMapping(value = "/{id}/like")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "대댓글 좋아요 취소 API", description = "특정 유저가 특정 대댓글에 좋아요를 누른 걸 취소하는 Api 입니다.")
	public ApiResponse<Void> cancelLikeChildComment(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		childCommentService.cancelLikeChildComment(userDetails.getUser().getId(), id);
		return ApiResponse.success();
	}
}
