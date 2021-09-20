package net.causw.adapter.web;

import net.causw.application.BoardService;
import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.BoardUpdateRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BoardResponseDto findById(@PathVariable String id) {
        return this.boardService.findById(id);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public BoardResponseDto create(@AuthenticationPrincipal String creatorId,
                                   @RequestBody BoardCreateRequestDto boardCreateRequestDto) {
        return this.boardService.create(creatorId, boardCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BoardResponseDto update(@AuthenticationPrincipal String updaterId,
                                   @PathVariable String id,
                                   @RequestBody BoardUpdateRequestDto boardUpdateRequestDto) {
        return this.boardService.update(updaterId, id, boardUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BoardResponseDto delete(@AuthenticationPrincipal String deleterId,
                                   @PathVariable String id) {
        return this.boardService.delete(deleterId, id);
    }
}
