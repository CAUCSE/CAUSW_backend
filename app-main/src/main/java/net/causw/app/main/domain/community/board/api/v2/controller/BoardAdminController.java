package net.causw.app.main.domain.community.board.api.v2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.board.api.v2.dto.request.BoardSearchCondition;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigEditResponse;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardConfigListResponse;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardAdminListMapper;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardConfigEditResponseMapper;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardSearchConditionMapper;
import net.causw.app.main.domain.community.board.service.BoardService;
import net.causw.app.main.domain.community.board.service.dto.result.BoardConfigListResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/boards")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
public class BoardAdminController {

	private final BoardService boardService;
	private final BoardAdminListMapper boardAdminListMapper;
	private final BoardSearchConditionMapper boardSearchConditionMapper;
	private final BoardConfigEditResponseMapper boardConfigEditResponseMapper;

	@GetMapping
	public ApiResponse<BoardConfigListResponse> getBoardAdminList(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@ModelAttribute BoardSearchCondition boardSearchCondition) {
		BoardConfigListResult allBoardList = boardService
			.getAllBoardList(boardSearchConditionMapper.toServiceDto(boardSearchCondition));
		BoardConfigListResponse response = boardAdminListMapper.toResponse(allBoardList);

		return ApiResponse.success(response);
	}

	@GetMapping("/{boardId}")
	public ApiResponse<BoardConfigEditResponse> editBoardAdmin(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String boardId
		) {
		BoardConfigEditResponse response = boardConfigEditResponseMapper.toResponse(
			boardService.getBoardConfigEditInfo(boardId)
		);

		return ApiResponse.success(response);
	}
}
