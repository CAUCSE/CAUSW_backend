package net.causw.adapter.web;

import net.causw.application.CommentService;
import net.causw.application.dto.CommentCreateRequestDto;
import net.causw.application.dto.CommentResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto findById(@PathVariable String id) {
        return this.commentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto create(
            @AuthenticationPrincipal String creatorId,
            @RequestBody CommentCreateRequestDto commentCreateRequestDto
    ) {
        return this.commentService.create(creatorId, commentCreateRequestDto);
    }
}