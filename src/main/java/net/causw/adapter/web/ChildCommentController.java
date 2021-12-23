package net.causw.adapter.web;

import net.causw.application.ChildCommentService;
import net.causw.application.dto.ChildCommentCreateRequestDto;
import net.causw.application.dto.ChildCommentResponseDto;
import net.causw.application.dto.ChildCommentUpdateRequestDto;
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
@RequestMapping("/api/v1/child-comments")
public class ChildCommentController {
    private final ChildCommentService childCommentService;

    public ChildCommentController(ChildCommentService childCommentService) {
        this.childCommentService = childCommentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChildCommentResponseDto create(
            @AuthenticationPrincipal String creatorId,
            @RequestBody ChildCommentCreateRequestDto childCommentCreateRequestDto
    ) {
        return this.childCommentService.create(creatorId, childCommentCreateRequestDto);
    }

    @GetMapping(params = "parentCommentId")
    @ResponseStatus(value = HttpStatus.OK)
    public Page<ChildCommentResponseDto> findAll(
            @AuthenticationPrincipal String userId,
            @RequestParam String parentCommentId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.childCommentService.findAll(userId, parentCommentId, pageNum);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public ChildCommentResponseDto update(
            @AuthenticationPrincipal String updaterId,
            @PathVariable String id,
            @RequestBody ChildCommentUpdateRequestDto childCommentUpdateRequestDto
    ) {
        return this.childCommentService.update(updaterId, id, childCommentUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public ChildCommentResponseDto delete(
            @AuthenticationPrincipal String deleterId,
            @PathVariable String id
    ) {
        return this.childCommentService.delete(deleterId, id);
    }
}
