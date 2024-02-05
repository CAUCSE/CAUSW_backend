package net.causw.adapter.web;

import net.causw.application.post.PostService;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.PostCreateRequestDto;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostUpdateRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.findById(loginUserId, id);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public BoardPostsResponseDto findAll(
            @RequestParam String boardId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.findAll(loginUserId, boardId, pageNum);
    }

    @GetMapping("/search")
    @ResponseStatus(value = HttpStatus.OK)
    public BoardPostsResponseDto search(
            @RequestParam String boardId,
            @RequestParam(defaultValue = "title") String option,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.search(loginUserId, boardId, option, keyword, pageNum);
    }

    @GetMapping("/app/notice")
    @ResponseStatus(value = HttpStatus.OK)
    public BoardPostsResponseDto findAllAppNotice(
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.findAllAppNotice(loginUserId, pageNum);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public PostResponseDto create(
            @RequestBody PostCreateRequestDto postCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.create(loginUserId, postCreateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public PostResponseDto delete(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.delete(loginUserId, id);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public PostResponseDto update(
            @PathVariable String id,
            @RequestBody PostUpdateRequestDto postUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.update(
                loginUserId,
                id,
                postUpdateRequestDto
        );
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(value = HttpStatus.OK)
    public PostResponseDto restore(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.restore(
                loginUserId,
                id
        );
    }
}
