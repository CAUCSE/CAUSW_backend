package net.causw.app.main.domain.community.board.api.v2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardConfigUpdateRequest;
import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardCreateRequest;
import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardOrderUpdateRequest;
import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardSearchCondition;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigEditResponse;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigListResponse;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardAdminMapper;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardOrderUpdateRequestMapper;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardSearchConditionMapper;
import net.causw.app.main.domain.community.board.service.BoardAdminService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/boards")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "관리자 게시판 api", description = "관리자 게시판 관리 API")
public class BoardAdminController {

	private final BoardAdminService boardAdminService;
	private final BoardAdminMapper boardAdminMapper;
	private final BoardSearchConditionMapper boardSearchConditionMapper;
	private final BoardOrderUpdateRequestMapper boardOrderUpdateRequestMapper;

	@GetMapping
	@Operation(summary = "게시판 목록 조회", description = "게시판 목록을 조회한다.")
	public ApiResponse<BoardConfigListResponse> getBoardAdminList(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@ModelAttribute BoardSearchCondition boardSearchCondition) {
		BoardConfigListResponse response = boardAdminMapper.toListResponse(
			boardAdminService.getAllBoardList(boardSearchConditionMapper.toServiceDto(boardSearchCondition)));

		return ApiResponse.success(response);
	}

	@GetMapping("/{boardId}")
	@Operation(summary = "게시판 설정 조회", description = "특정 게시판의 설정 정보를 조회한다.")
	public ApiResponse<BoardConfigEditResponse> editBoardAdmin(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String boardId) {
		BoardConfigEditResponse response = boardAdminMapper.toEditResponse(
			boardAdminService.getBoardConfigEditInfo(boardId));

		return ApiResponse.success(response);
	}

	@PostMapping
	@Operation(summary = "게시판 생성", description = "새로운 게시판을 생성한다.")
	public ApiResponse<Void> createBoard(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody BoardCreateRequest request) {
		boardAdminService.createBoard(
			boardAdminMapper.toBoardPart(request),
			boardAdminMapper.toBoardConfigPart(request),
			request.adminUserIds());

		return ApiResponse.success();
	}

	@PutMapping("/{boardId}")
	@Operation(summary = "게시판 설정 수정", description = "특정 게시판의 설정 정보를 수정한다.")
	public ApiResponse<Void> updateBoard(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String boardId,
		@Valid @RequestBody BoardConfigUpdateRequest request) {
		if (!boardId.equals(request.boardId())) {
			throw new BaseRunTimeV2Exception(
				BoardErrorCode.BOARD_NOT_FOUND);
		}
		boardAdminService.updateBoard(
			boardId,
			boardAdminMapper.toBoardPart(request),
			boardAdminMapper.toBoardConfigPart(request),
			request.adminUserIds());

		return ApiResponse.success();
	}

	@DeleteMapping("/{boardId}")
	@Operation(summary = "게시판 삭제", description = "특정 게시판을 삭제한다.")
	public ApiResponse<Void> deleteBoard(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String boardId) {

		boardAdminService.deleteBoard(boardId);

		return ApiResponse.success();
	}

	@PatchMapping("/orders")
	@Operation(summary = "게시판 순서 변경", description = "게시판의 표시 순서를 변경한다.")
	public ApiResponse<Void> updateBoardOrder(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody BoardOrderUpdateRequest request) {
		boardAdminService.updateBoardOrder(boardOrderUpdateRequestMapper.toCommand(request));
		return ApiResponse.success();
	}
}
