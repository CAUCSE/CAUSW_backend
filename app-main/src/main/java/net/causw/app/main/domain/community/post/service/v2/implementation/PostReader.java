package net.causw.app.main.domain.community.post.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.post.repository.query.PostQueryRepository;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * Post 도메인의 읽기 전용 작업을 담당하는 컴포넌트
 * Repository에서 데이터를 조회하고 예외를 처리합니다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReader {
	private final PostRepository postRepository;
	private final PostQueryRepository postQueryRepository;

	/**
	 * Post ID로 Post를 조회합니다. (Board와 함께 Fetch Join)
	 *
	 * @param postId Post ID
	 * @return Post Entity
	 */
	public Post findById(String postId) {
		return postRepository.findById(postId)
			.orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
	}

	/**
	 * Post ID로 삭제되지 않은 Post를 조회합니다.
	 *
	 * @param postId Post ID
	 * @return Post Entity
	 */
	public Post findByIdAndNotDeleted(String postId) {
		return postRepository.findByIdAndIsDeletedFalse(postId)
			.orElseThrow(PostErrorCode.POST_NOT_FOUND::toBaseException);
	}
}
