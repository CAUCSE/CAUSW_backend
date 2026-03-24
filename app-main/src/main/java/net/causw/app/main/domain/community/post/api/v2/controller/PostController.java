package net.causw.app.main.domain.community.post.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.community.post.api.v2.dto.request.PostCreateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostListCondition;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostUpdateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostCreateResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostListResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostUpdateResponse;
import net.causw.app.main.domain.community.post.api.v2.mapper.PostDtoMapper;
import net.causw.app.main.domain.community.post.service.v2.LikePostService;
import net.causw.app.main.domain.community.post.service.v2.PostService;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Post Public v2", description = "게시글 관련 API")
@RequestMapping("/api/v2/posts")
public class PostController {

	private final PostService postService;
	private final LikePostService likePostService;
	private final PostDtoMapper postDtoMapper;

	@PostMapping(value = "/{id}/like")
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "게시글 좋아요 저장 API", description = "특정 유저가 특정 게시글에 좋아요를 누른 걸 저장하는 Api 입니다.")
	public ApiResponse<Void> likePost(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		this.likePostService.likePost(userDetails.getUser().getId(), id);
		return ApiResponse.success();
	}

	@DeleteMapping(value = "/{id}/like")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "게시글 좋아요 취소 API", description = "특정 유저가 특정 게시글에 좋아요를 누른 걸 취소하는 Api 입니다.")
	public ApiResponse<Void> cancelLikePost(
		@PathVariable("id") String id,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		this.likePostService.cancelLikePost(userDetails.getUser().getId(), id);
		return ApiResponse.success();
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
	public ApiResponse<PostCreateResponse> create(
		@Valid @RequestPart(value = "postCreateRequest") PostCreateRequest postCreateRequest,
		@RequestPart(value = "attachImageList", required = false) List<MultipartFile> images,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		PostCreateResult result = postService
			.create(postDtoMapper.toCommand(postCreateRequest, userDetails.getUser(), images));
		return ApiResponse.success(postDtoMapper.toResponse(result));
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "게시글 목록 조회", description = "게시글 목록을 커서 기반 페이징으로 조회합니다.")
	public ApiResponse<PostListResponse> getPosts(
		@ModelAttribute PostListCondition condition,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		PostListQuery query = postDtoMapper.toListQuery(condition, userDetails.getUser());
		PostListResult result = postService.getPosts(query);
		return ApiResponse.success(postDtoMapper.toListResponse(result));
	}

	@GetMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "내가 쓴 글 목록 조회", description = "로그인한 사용자가 작성한 게시글 목록을 커서 기반으로 조회합니다. 게시글 목록 조회와 동일한 형식(posts, nextCursor)으로 반환합니다.")
	public ApiResponse<PostListResponse> getPostsWrittenByMe(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", required = false) Integer size) {
		PostListResult result = postService.getPostsWrittenByUser(userDetails.getUser(), cursor, size);
		return ApiResponse.success(postDtoMapper.toListResponse(result));
	}

	@GetMapping("/me/liked")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "내가 좋아요 누른 글 목록 조회", description = "로그인한 사용자가 좋아요를 누른 게시글 목록을 커서 기반으로 조회합니다. 게시글 목록 조회와 동일한 형식(posts, nextCursor)으로 반환합니다.")
	public ApiResponse<PostListResponse> getPostsLikedByMe(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", required = false) Integer size) {
		PostListResult result = postService.getPostsLikedByUser(userDetails.getUser(), cursor, size);
		return ApiResponse.success(postDtoMapper.toListResponse(result));
	}

	@GetMapping("/me/commented")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "내가 댓글 단 글 목록 조회", description = "로그인한 사용자가 댓글을 작성한 게시글 목록을 커서 기반으로 조회합니다. 게시글 목록 조회와 동일한 형식(posts, nextCursor)으로 반환합니다.")
	public ApiResponse<PostListResponse> getPostsCommentedByMe(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", required = false) Integer size) {
		PostListResult result = postService.getPostsCommentedByUser(userDetails.getUser(), cursor, size);
		return ApiResponse.success(postDtoMapper.toListResponse(result));
	}

	@GetMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "게시글 단건 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
	public ApiResponse<PostResponse> getPost(
		@PathVariable String postId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		PostDetailQuery query = postDtoMapper.toDetailQuery(postId, userDetails.getUser());
		PostDetailResult result = postService.getPostDetail(query);
		return ApiResponse.success(postDtoMapper.toDetailResponse(result));
	}

	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (소프트 삭제)")
	public ApiResponse<Void> delete(
		@PathVariable String postId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		postService.deletePost(userDetails.getUser(), postId);
		return ApiResponse.success();
	}

	@PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "게시글 수정", description = "게시글의 내용과 첨부 이미지를 수정합니다.")
	public ApiResponse<PostUpdateResponse> update(
		@PathVariable String postId,
		@Valid @RequestPart(value = "postUpdateRequest") PostUpdateRequest postUpdateRequest,
		@RequestPart(value = "attachImageList", required = false) List<MultipartFile> images,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		PostUpdateResult result = postService
			.update(postDtoMapper.toUpdateCommand(postId, postUpdateRequest, userDetails.getUser(), images));
		return ApiResponse.success(postDtoMapper.toUpdateResponse(result));
	}
}
