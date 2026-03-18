package net.causw.app.main.domain.community.board.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.community.board.api.v2.dto.response.BoardReadableListResponse;
import net.causw.app.main.domain.community.board.api.v2.mapper.BoardReadableMapper;
import net.causw.app.main.domain.community.board.service.v2.BoardService;
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

	@GetMapping("/available")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "이용 가능한 게시판 목록", description = "현재 사용자가 이용 가능한 게시판의 id, name 목록을 표시 순서대로 반환합니다.")
	public ApiResponse<BoardReadableListResponse> getAvailableBoards(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(
			boardReadableMapper.toReadableListResponse(boardService.getReadableBoards(userDetails.getUser().getId())));
	}
}
