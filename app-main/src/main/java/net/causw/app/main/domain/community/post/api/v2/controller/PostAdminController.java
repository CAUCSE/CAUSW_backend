package net.causw.app.main.domain.community.post.api.v2.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.post.api.v2.dto.request.PostAdminListCondition;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostCategoryUpdateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.request.PostStatusUpdateRequest;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostAdminDetailResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.PostAdminSummaryResponse;
import net.causw.app.main.domain.community.post.api.v2.dto.response.UncategorizedPostResponse;
import net.causw.app.main.domain.community.post.api.v2.mapper.PostAdminMapper;
import net.causw.app.main.domain.community.post.service.PostAdminService;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/posts")
@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
@Tag(name = "Post Admin v2", description = "관리자 게시글 관리 API")
public class PostAdminController {

	private final PostAdminService postAdminService;
	private final PostAdminMapper postAdminMapper;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "관리자 게시물 목록 조회", description = "게시판, 성격, 상태, 검색어 조건으로 게시물을 조회합니다.")
	public ApiResponse<PageResponse<PostAdminSummaryResponse>> getPosts(
		@ParameterObject PostAdminListCondition condition,
		@ParameterObject @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ApiResponse.success(PageResponse.from(
			postAdminService.getPosts(postAdminMapper.toQuery(condition), pageable)
				.map(postAdminMapper::toResponse)));
	}

	@GetMapping("/{postId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "관리자 게시물 상세 조회", description = "삭제되거나 숨겨진 게시물을 포함해 상세 정보를 조회합니다.")
	public ApiResponse<PostAdminDetailResponse> getPostDetail(
		@Parameter(description = "게시글 ID") @PathVariable String postId) {
		return ApiResponse.success(postAdminMapper.toResponse(postAdminService.getPostDetail(postId)));
	}

	@PatchMapping("/{postId}/status")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "게시물 상태 변경", description = "게시물을 공개, 숨김 또는 삭제 상태로 변경합니다.")
	public ApiResponse<Void> changeStatus(
		@Parameter(description = "게시글 ID") @PathVariable String postId,
		@Valid @RequestBody PostStatusUpdateRequest request) {
		postAdminService.changeStatus(postId, request.status());
		return ApiResponse.success();
	}

	@PatchMapping("/{postId}/category")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "게시글 성격 수정", description = "크롤링 소식 게시글의 성격을 수동으로 지정합니다. null을 전달하면 미분류로 되돌립니다.")
	public ApiResponse<Void> updateCategory(
		@Parameter(description = "게시글 ID") @PathVariable String postId,
		@Valid @RequestBody PostCategoryUpdateRequest request) {
		postAdminService.updateCategory(postId, request.category());
		return ApiResponse.success();
	}

	@GetMapping("/uncategorized")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "성격 미분류 게시글 목록 조회", description = "성격이 지정되지 않아 확인이 필요한 크롤링 게시글을 조회합니다. 관리자가 성격을 지정하면 목록에서 제외됩니다.")
	public ApiResponse<PageResponse<UncategorizedPostResponse>> getUncategorizedPosts(
		@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
		return ApiResponse.success(
			PageResponse.from(
				postAdminService.getUncategorizedPosts(pageable)
					.map(result -> new UncategorizedPostResponse(
						result.postId(), result.title(), result.boardName(), result.createdAt()))));
	}
}
