package net.causw.app.main.domain.community.post.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.community.post.api.v2.dto.request.PostCreateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostListCondition;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostUpdateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostCreateResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostListResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostUpdateResponse;
import net.causw.app.main.domain.community.post.api.v2.mapper.PostDtoMapper;
import net.causw.app.main.domain.community.post.service.v2.PostService;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/posts")
public class PostController {

	private final PostService postService;
	private final PostDtoMapper postDtoMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
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
	public ApiResponse<PostListResponse> getPosts(
		@ModelAttribute PostListCondition condition,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		PostListQuery query = postDtoMapper.toListQuery(condition, userDetails.getUser());
		PostListResult result = postService.getPosts(query);
		return ApiResponse.success(postDtoMapper.toListResponse(result));
	}

	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> delete(
		@PathVariable String postId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		postService.deletePost(userDetails.getUser(), postId);
		return ApiResponse.success();
	}

	@PutMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
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
