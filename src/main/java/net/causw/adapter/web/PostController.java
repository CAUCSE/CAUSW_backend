package net.causw.adapter.web;

import net.causw.application.PostService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /* TODO : Refactoring & Implementation
    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public PostDetailDto findById(@PathVariable String id) {
        return this.postService.findById(id);
    }
     */
}
