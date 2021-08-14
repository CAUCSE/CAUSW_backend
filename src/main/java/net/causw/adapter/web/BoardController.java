package net.causw.adapter.web;

import net.causw.application.BoardService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    /* TODO : Refactoring & Implementation
    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public BoardDetailDto findById(@PathVariable String id) {
        return this.boardService.findById(id);
    }
     */
}
