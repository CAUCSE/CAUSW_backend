package net.causw.app.main.domain.community.post.service.v2.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// [위반 4] RuntimeException 직접 throw + System.out.println (섹션 7: 예외/로깅 규칙)
// Custom ErrorCode(PostErrorCode.POST_NOT_FOUND::toBaseException) 사용해야 한다.
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostConventionViolationReader {

    private final PostRepository postRepository;

    public Post findById(String postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> {
                System.out.println("Post not found: " + postId); // ❌ System.out.println 금지
                throw new RuntimeException("게시글을 찾을 수 없습니다: " + postId); // ❌ RuntimeException 직접 사용
            });
    }

    public Post findByIdForAdmin(String postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found")); // ❌ 표준 예외 직접 사용

        if (post.getIsDeleted()) {
            System.out.println("deleted post accessed: " + postId); // ❌ System.out.println
        }
        return post;
    }
}
