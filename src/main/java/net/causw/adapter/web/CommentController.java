package net.causw.adapter.web;

import net.causw.application.comment.CommentService;
import net.causw.application.dto.comment.CommentCreateRequestDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto create(
            @AuthenticationPrincipal String creatorId,
            @RequestBody CommentCreateRequestDto commentCreateRequestDto
    ) {
        return this.commentService.create(creatorId, commentCreateRequestDto);
    }

    @GetMapping(params = "postId")
    @ResponseStatus(value = HttpStatus.OK)
    public Page<CommentResponseDto> findAll(
            @AuthenticationPrincipal String userId,
            @RequestParam String postId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.commentService.findAll(userId, postId, pageNum);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public CommentResponseDto update(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id,
            @RequestBody CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        return this.commentService.update(
                requestUserId,
                id,
                commentUpdateRequestDto
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public CommentResponseDto delete(
            @AuthenticationPrincipal String userId,
            @PathVariable String id
    ) {
        return this.commentService.delete(userId, id);
    }
}