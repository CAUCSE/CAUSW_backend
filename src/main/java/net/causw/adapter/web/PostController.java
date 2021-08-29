package net.causw.adapter.web;

import net.causw.application.PostService;
import net.causw.application.dto.PostResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public PostResponseDto findById(@PathVariable String id) {
        return this.postService.findById(id);
    }
}
