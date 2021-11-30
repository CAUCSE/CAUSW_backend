package net.causw.adapter.web;

import net.causw.application.PostService;
import net.causw.application.dto.PostAllResponseDto;
import net.causw.application.dto.PostAllWithBoardResponseDto;
import net.causw.application.dto.PostCreateRequestDto;
import net.causw.application.dto.PostResponseDto;
import net.causw.application.dto.PostUpdateRequestDto;
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
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public PostResponseDto findById(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.postService.findById(requestUserId, id);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public PostAllWithBoardResponseDto findAll(
            @AuthenticationPrincipal String requestUserId,
            @RequestParam String boardId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.postService.findAll(requestUserId, boardId, pageNum);
    }

    @GetMapping("/app/notice")
    @ResponseStatus(value = HttpStatus.OK)
    public PostAllWithBoardResponseDto findAllAppNotice(
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.postService.findAllAppNotice(pageNum);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public PostResponseDto create(
            @AuthenticationPrincipal String requestUserId,
            @RequestBody PostCreateRequestDto postCreateRequestDto
    ) {
        return this.postService.create(requestUserId, postCreateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public PostResponseDto delete(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.postService.delete(requestUserId, id);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public PostResponseDto update(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id,
            @RequestBody PostUpdateRequestDto postUpdateRequestDto
    ) {
        return this.postService.update(
                requestUserId,
                id,
                postUpdateRequestDto
        );
    }
}
