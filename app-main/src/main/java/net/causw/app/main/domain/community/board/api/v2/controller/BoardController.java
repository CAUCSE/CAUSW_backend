package net.causw.app.main.domain.community.board.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardReadableListResponse;
import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardWritableListResponse;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardReadableMapper;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardWritableMapper;
import net.causw.app.main.domain.community.board.entity.BoardGroup;
import net.causw.app.main.domain.community.board.service.BoardService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/boards")
@Tag(name = "Board v2 Public", description = "게시판 조회 API")
public class BoardController {

	private final BoardService boardService;
	private final BoardReadableMapper boardReadableMapper;
	private final BoardWritableMapper boardWritableMapper;

	@GetMapping("/available")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "이용 가능한 게시판 목록", description = "현재 사용자가 이용 가능한 게시판의 id, name 목록을 표시 순서대로 반환합니다.\n"
		+ "파라미터로 boardGroup 전달 시 해당 그룹(소식 = NOTICE, 소통 = COMMUNITY)에 맞는 게시판 목록을 반환합니다. 미지정 시 전체 게시판을 반환합니다. 기존 isTab=true 요청도 지원합니다.")
	public ApiResponse<BoardReadableListResponse> getAvailableBoards(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "boardGroup", required = false) BoardGroup boardGroup,
		@RequestParam(name = "isTab", defaultValue = "false") boolean isTab) {
		BoardGroup resolvedBoardGroup = boardGroup != null ? boardGroup : (isTab ? BoardGroup.NOTICE : null);
		return ApiResponse.success(
			boardReadableMapper
				.toReadableListResponse(boardService.getReadableBoards(userDetails.getUser().getId(), resolvedBoardGroup)));
	}

	@GetMapping("/writable")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "쓰기 가능한 게시판 목록", description = "현재 사용자가 쓰기 가능한 게시판의 id, name 목록을 표시 순서대로 반환합니다.\n"
		+ "파라미터로 boardGroup 전달 시 해당 그룹(소식 = NOTICE, 소통 = COMMUNITY)에 맞는 게시판 목록을 반환합니다. 미지정 시 전체 게시판을 반환합니다.")
	public ApiResponse<BoardWritableListResponse> getWritableBoards(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "boardGroup", required = false) BoardGroup boardGroup) {
		return ApiResponse.success(
			boardWritableMapper
				.toWritableListResponse(boardService.getWritableBoards(userDetails.getUser().getId(), boardGroup)));
	}
}
