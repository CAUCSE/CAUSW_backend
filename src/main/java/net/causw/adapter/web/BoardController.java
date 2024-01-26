package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
import net.causw.application.board.BoardService;
import net.causw.application.dto.board.BoardCreateRequestDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.board.BoardUpdateRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    @ApiOperation(value = "게시판 검색 API (미완료/사용가능)", notes = "userId 값은 token 으로 적용되었습니다")
    public List<BoardResponseDto> findAll() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);
        return this.boardService.findAll(userId);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "게시판 생성 API (미완료/사용가능)", notes = "circleId는 현재 존재하는 circleId를 적용해야 합니다")
    public BoardResponseDto create(
            @RequestBody BoardCreateRequestDto boardCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String creatorId = ((String) principal);
        return this.boardService.create(creatorId, boardCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 업데이트 API (미완료/사용가능)", notes = "id 에는 board id 값을 넣어주세요")
    public BoardResponseDto update(
            @PathVariable String id,
            @RequestBody BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String updaterId = ((String) principal);
        return this.boardService.update(updaterId, id, boardUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 삭제 API (미완료/사용가능)", notes = "id 에는 board id 값을 넣어주세요")
    public BoardResponseDto delete(

            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String deleterId = ((String) principal);
        return this.boardService.delete(deleterId, id);
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(value =  HttpStatus.OK)
    @ApiOperation(value = "게시판 복구 API (미완료/사용가능)", notes = "id 에는 board id 값을 넣어주세요")
    public BoardResponseDto restore(

            @PathVariable String id
    ){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String restorerId = ((String) principal);
        return this.boardService.restore(restorerId, id);
    }
}
