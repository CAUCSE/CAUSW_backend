package net.causw.app.main.domain.community.post.service.v2;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.post.service.v2.dto.PostServiceDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// [위반 2] Service가 Reader 없이 Repository 직접 의존 (섹션 3: Service/Implementation 분리 규칙)
// Service는 PostReader를 통해서만 Post를 조회해야 한다.
@Service
@RequiredArgsConstructor
public class PostConventionViolationService {

    private final PostRepository postRepository; // ❌ Reader/Writer 없이 Repository 직접 의존

    @Transactional(readOnly = true)
    public PostServiceDto.PostDetail findPost(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(); // ❌ PostReader.findById() 사용해야 함
        return PostServiceDto.PostDetail.from(post);
    }

    @Transactional
    public void deletePost(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(); // ❌ PostReader 미사용
        post.setIsDeleted(true);
        // postRepository.save(post); ← Writer에서 처리해야 함
    }
}
