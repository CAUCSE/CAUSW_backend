package net.causw.app.main.domain.community.post.service.v2.implementation;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// [위반 5] 소프트 삭제 필터 없는 조회 — IncludeDeleted 접미사 누락 (섹션 6: 소프트 삭제 조회 규칙)
// 메서드명이 findById/findAllByBoardId이면 isDeleted=false 필터가 묵시적으로 적용되어야 한다.
// 삭제된 데이터까지 포함해서 조회할 경우 반드시 IncludeDeleted 접미사를 붙여야 한다.
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostConventionViolationSoftDeleteReader {

    private final PostRepository postRepository;

    // ❌ 메서드명은 findById이지만 isDeleted 필터 없이 전체 조회
    //    → 삭제된 게시글도 반환될 수 있음
    //    → findByIdIncludeDeleted(postId) 로 이름을 바꾸거나,
    //       isDeleted=false 조건을 추가해야 함
    public Post findById(String postId) {
        return postRepository.findById(postId) // ❌ isDeleted 조건 없음
            .orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
    }

    // ❌ 마찬가지로 boardId 기준 전체 조회인데 삭제 필터 없음
    //    → findAllByBoardIdIncludeDeleted() 이거나 isDeleted=false 필터 추가 필요
    public List<Post> findAllByBoardId(String boardId) {
        return postRepository.findAllByBoardId(boardId); // ❌ isDeleted 조건 없음
    }
}
