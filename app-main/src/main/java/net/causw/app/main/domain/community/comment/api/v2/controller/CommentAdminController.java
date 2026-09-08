package net.causw.app.main.domain.community.comment.api.v2.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
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

import net.causw.app.main.domain.community.comment.api.v2.dto.request.CommentStatusUpdateRequest;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.CommentAdminResponse;
import net.causw.app.main.domain.community.comment.api.v2.mapper.CommentAdminMapper;
import net.causw.app.main.domain.community.comment.service.CommentAdminService;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "Comment Admin v2", description = "관리자 댓글 관리 API")
public class CommentAdminController {
	private final CommentAdminService commentAdminService;
	private final CommentAdminMapper commentAdminMapper;

	@GetMapping("/posts/{postId}/comments")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "관리자 댓글 목록 조회", description = "게시물의 삭제 댓글과 대댓글을 포함해 조회합니다.")
	public ApiResponse<PageResponse<CommentAdminResponse>> getComments(
		@Parameter(description = "게시글 ID") @PathVariable String postId,
		@ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
		return ApiResponse.success(PageResponse.from(
			commentAdminService.getComments(postId, pageable).map(commentAdminMapper::toResponse)));
	}

	@PatchMapping("/comments/{commentId}/status")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "댓글 상태 변경", description = "댓글을 공개 또는 삭제 상태로 변경합니다.")
	public ApiResponse<Void> changeStatus(
		@Parameter(description = "댓글 ID") @PathVariable String commentId,
		@Valid @RequestBody CommentStatusUpdateRequest request) {
		commentAdminService.changeStatus(commentId, request.status());
		return ApiResponse.success();
	}
}
