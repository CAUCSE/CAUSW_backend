package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
import net.causw.application.board.BoardService;
import net.causw.application.dto.board.BoardCreateRequestDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.board.BoardUpdateRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 검색 API (미완료/사용가능)", notes = "전체 게시판을 불러오는 api로 관리자 권한을 가진 경우 삭제된 게시판도 확인할 수 있습니다.\nvalidation 오류 응답값은 추후 추가 예정입니다")
    public List<BoardResponseDto> findAllBoard() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);
        return this.boardService.findAllBoard(userId);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "게시판 생성 API (미완료/사용가능)", notes = "circleId는 현재 존재하는 circleId를 적용해야 합니다\n validation 오류 응답값은 추후 추가 예정입니다")
    public BoardResponseDto createBoard(
            @RequestBody BoardCreateRequestDto boardCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String creatorId = ((String) principal);
        return this.boardService.createBoard(creatorId, boardCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 업데이트 API (미완료/사용가능)", notes = "id 에는 board id 값을 넣어주세요\n validation 오류 응답값은 추후 추가 예정입니다")
    public BoardResponseDto updateBoard(
            @PathVariable String id,
            @RequestBody BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String updaterId = ((String) principal);
        return this.boardService.updateBoard(updaterId, id, boardUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 삭제 API (미완료/사용가능)", notes = "id 에는 board id 값을 넣어주세요\n validation 오류 응답값은 추후 추가 예정입니다")
    public BoardResponseDto deleteBoard(

            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String deleterId = ((String) principal);
        return this.boardService.deleteBoard(deleterId, id);
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(value =  HttpStatus.OK)
    @ApiOperation(value = "게시판 복구 API (미완료/사용가능)", notes = "id 에는 board id 값을 넣어주세요\n validation 오류 응답값은 추후 추가 예정입니다")
    public BoardResponseDto restoreBoard(

            @PathVariable String id
    ){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String restorerId = ((String) principal);
        return this.boardService.restoreBoard(restorerId, id);
    }
}
