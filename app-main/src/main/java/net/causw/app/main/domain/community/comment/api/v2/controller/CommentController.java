package net.causw.app.main.domain.community.comment.api.v2.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentCreateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentUpdateRequestDto;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentResponseDto;
import net.causw.app.main.domain.community.comment.service.CommentService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;
import net.causw.global.constant.StaticValue;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/comments")
public class CommentController {
	private final CommentService commentService;

	@GetMapping(params = "postId")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "댓글 조회 API", description = "해당 게시글의 전체 댓글을 불러오는 api입니다.")
	public ApiResponse<PageResponse<CommentResponseDto>> findAllComments(
		@RequestParam("postId") String postId,
		@RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		PageRequest pageRequest = PageRequest.of(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE);

		PageResponse<CommentResponseDto> response = PageResponse.from(
			commentService.findAllComments(userDetails.getUser().getId(), postId, pageRequest));

		return ApiResponse.success(response);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "댓글 생성 API", description = "댓글을 생성하는 api입니다.")
	public ApiResponse<CommentResponseDto> createComment(
		@Valid @RequestBody CommentCreateRequestDto commentCreateRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		CommentResponseDto response = commentService.createComment(userDetails.getUser().getId(),
			commentCreateRequestDto);

		return ApiResponse.success(response);
	}

	@PutMapping(value = "/{id}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "댓글 수정 API", description = "댓글을 수정하는 api입니다.")
	public ApiResponse<CommentResponseDto> updateComment(
		@PathVariable("id") String id,
		@Valid @RequestBody CommentUpdateRequestDto commentUpdateRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		CommentResponseDto response = commentService.updateComment(userDetails.getUser().getId(), id,
			commentUpdateRequestDto);

		return ApiResponse.success(response);
	}

	@DeleteMapping(value = "/{id}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "댓글 삭제 API", description = "댓글을 삭제하는 api입니다.")
	public ApiResponse<CommentResponseDto> deleteComment(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		CommentResponseDto response = commentService.deleteComment(userDetails.getUser().getId(), id);

		return ApiResponse.success(response);
	}

	@PostMapping(value = "/{id}/like")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "댓글 좋아요 저장 API", description = "특정 유저가 특정 댓글에 좋아요를 누른 걸 저장하는 Api 입니다.")
	public void likeComment(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		commentService.likeComment(userDetails.getUser().getId(), id);
	}

	@DeleteMapping(value = "/{id}/like")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "댓글 좋아요 취소 API", description = "특정 유저가 특정 댓글에 좋아요를 누른 걸 취소하는 Api 입니다.")
	public void cancelLikeComment(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		commentService.cancelLikeComment(userDetails.getUser().getId(), id);
	}

}