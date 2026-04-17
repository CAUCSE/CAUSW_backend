package net.causw.app.main.domain.community.post.api.v2.controller;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// [위반 1] Controller -> Repository 직접 호출 (섹션 1: 아키텍처 계층 규칙)
// Controller는 Service만 의존해야 한다. Repository 직접 참조 금지.
@RestController
@RequestMapping("/api/v2/posts/violation")
@RequiredArgsConstructor
public class PostConventionViolationController {

    private final PostRepository postRepository; // ❌ Controller가 Repository 직접 의존

    @GetMapping("/{id}")
    public Post getPost(@PathVariable String id) {
        return postRepository.findById(id).orElseThrow(); // ❌ Entity 직접 반환, orElseThrow 인자 없음
    }
}
