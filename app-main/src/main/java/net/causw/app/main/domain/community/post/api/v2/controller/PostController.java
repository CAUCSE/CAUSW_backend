package net.causw.app.main.domain.community.post.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.community.post.api.v2.dto.request.PostCreateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostCreateResponse;
import net.causw.app.main.domain.community.post.api.v2.mapper.PostDtoMapper;
import net.causw.app.main.domain.community.post.service.v2.PostService;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
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
}
