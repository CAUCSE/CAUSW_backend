package net.causw.app.main.domain.community.post.service.v2.implementation;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.post.repository.query.PostQueryRepository;
import net.causw.app.main.domain.community.post.repository.query.PostQueryResult;
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

	/**
	 * 커서 기반 페이징으로 게시글 목록을 조회합니다.
	 *
	 * @param boardIds 게시판 ID 목록 (null이면 전체 게시판)
	 * @param cursorCreatedAt 커서 (마지막 게시글의 createdAt)
	 * @param cursorId 커서 (마지막 게시글의 ID)
	 * @param size 조회할 개수
	 * @param keyword 검색 키워드
	 * @return 게시글 목록 Slice
	 */
	public Slice<PostQueryResult> findPostsWithCursor(
		List<String> boardIds,
		String cursorCreatedAt,
		String cursorId,
		int size,
		String keyword) {
		return postQueryRepository.findPostsWithCursor(boardIds, cursorCreatedAt, cursorId, size, keyword);
	}

	/**
	 * 여러 게시글의 이미지 URL 목록을 조회합니다.
	 *
	 * @param postIds 게시글 ID 목록
	 * @return 게시글 ID를 키로, 이미지 URL 목록을 값으로 하는 맵
	 */
	public Map<String, List<String>> findPostImagesByPostIds(List<String> postIds) {
		return postQueryRepository.findPostImagesByPostIds(postIds);
	}
}
