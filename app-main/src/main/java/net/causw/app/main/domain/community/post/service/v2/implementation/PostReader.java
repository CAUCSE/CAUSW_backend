package net.causw.app.main.domain.community.post.service.v2.implementation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.post.repository.query.PostCursorResult;
import net.causw.app.main.domain.community.post.repository.query.PostQueryRepository;
import net.causw.app.main.domain.integration.crawled.entity.CrawledPostImage;
import net.causw.app.main.domain.integration.crawled.repository.CrawledPostImageRepository;
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
	private final CrawledPostImageRepository crawledPostImageRepository;

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
	 * 여러 Post ID를 한 번에 조회하고, ID를 키로 하는 Map으로 반환합니다.
	 * 각 Post는 Board와 함께 조회됩니다.
	 *
	 * @param postIds Post ID 목록
	 * @return Post ID → Post Entity Map
	 */
	public Map<String, Post> findPostMapByIds(Collection<String> postIds) {
		if (postIds == null || postIds.isEmpty()) {
			return Map.of();
		}

		return postRepository.findAllByIdInWithBoard(postIds).stream()
			.collect(Collectors.toMap(Post::getId, Function.identity()));
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
	 * 커서 기반 페이징으로 게시글 목록을 조회합니다. (V2용)
	 *
	 * @param boardIds 게시판 ID 목록 (null이면 전체 게시판)
	 * @param blockedUserIds 차단된 유저 ID 목록 (null이면 차단된 유저 없음)
	 * @param cursorCreatedAt 커서 (마지막 게시글의 createdAt)
	 * @param cursorId 커서 (마지막 게시글의 ID)
	 * @param size 조회할 개수
	 * @param keyword 검색 키워드
	 * @return 게시글 목록 Slice
	 */
	public Slice<PostCursorResult> findPostsWithCursor(
		List<String> boardIds,
		Set<String> blockedUserIds,
		String cursorCreatedAt,
		String cursorId,
		int size,
		String keyword) {
		return postQueryRepository.findPostsWithCursor(
			boardIds, blockedUserIds, cursorCreatedAt, cursorId, size, keyword);
	}

	/**
	 * 커서 기반 페이징으로 특정 유저가 댓글을 단 게시글 목록을 조회합니다. (V2용)
	 * @param userId 댓글을 단 유저 ID
	 * @param blockedUserIds 차단된 유저 ID 목록 (null이면 차단된 유저 없음)
	 * @param cursorCreatedAt 커서 (마지막 게시글의 createdAt)
	 * @param cursorId 커서 (마지막 게시글의 ID)
	 * @param size 조회할 개수
	 * @return 게시글 목록 Slice
	 */
	public Slice<PostCursorResult> findPostsCommentedByUserWithCursor(
		String userId,
		Set<String> blockedUserIds,
		String cursorCreatedAt,
		String cursorId,
		int size) {
		return postQueryRepository.findPostsCommentedByUserWithCursor(
			userId, blockedUserIds, cursorCreatedAt, cursorId, size);
	}

	/**
	 * 특정 사용자가 작성한 게시글을 커서 기반 페이징으로 조회합니다.
	 */
	public Slice<PostCursorResult> findPostsWrittenByUserWithCursor(
		String userId,
		String cursorCreatedAt,
		String cursorId,
		int size) {
		return postQueryRepository.findPostsWrittenByUserWithCursor(userId, cursorCreatedAt, cursorId, size);
	}

	/**
	 * 특정 사용자가 좋아요를 누른 게시글을 커서 기반 페이징으로 조회합니다.
	 */
	public Slice<PostCursorResult> findPostsLikedByUserWithCursor(
		String userId,
		Set<String> blockedUserIds,
		String cursorCreatedAt,
		String cursorId,
		int size) {
		return postQueryRepository.findPostsLikedByUserWithCursor(
			userId, blockedUserIds, cursorCreatedAt, cursorId, size);
	}

	/**
	 * 여러 게시글의 이미지 URL 목록을 조회합니다. (S3 업로드 이미지 + 크롤링 이미지 병합)
	 *
	 * @param postIds 게시글 ID 목록
	 * @return 게시글 ID를 키로, 이미지 URL 목록을 값으로 하는 맵
	 */
	public Map<String, List<String>> findPostImagesByPostIds(List<String> postIds) {
		// S3 업로드 이미지 조회
		Map<String, List<String>> imageMap = new java.util.HashMap<>(
			postQueryRepository.findPostImagesByPostIds(postIds));

		// 크롤링 이미지 병합
		List<CrawledPostImage> crawledImages = crawledPostImageRepository
			.findAllByPostIdInOrderByPostIdAscImageOrderAsc(postIds);
		for (CrawledPostImage crawledImage : crawledImages) {
			imageMap.computeIfAbsent(crawledImage.getPost().getId(), k -> new java.util.ArrayList<>())
				.add(crawledImage.getImageUrl());
		}

		return imageMap;
	}

	/**
	 * 단일 게시글의 이미지 URL 목록을 조회합니다.
	 *
	 * @param postId 게시글 ID
	 * @return 이미지 URL 목록
	 */
	public List<String> findPostImages(String postId) {
		Map<String, List<String>> result = postQueryRepository.findPostImagesByPostIds(List.of(postId));
		return result.getOrDefault(postId, List.of());
	}

	/**
	 * 특정 게시글의 댓글 개수를 조회합니다. (Comment + ChildComment)
	 *
	 * @param postId 게시글 ID
	 * @return 댓글 개수
	 */
	public long countComments(String postId) {
		return postQueryRepository.countCommentsByPostId(postId);
	}
}
