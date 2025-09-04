package net.causw.app.main.service.post;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostEntityService {
	private final PostRepository postRepository;

	public Post findByIdNotDeleted(String postId) {

		return postRepository.findByIdAndIsDeletedFalse(postId).orElseThrow(() ->
			new NotFoundException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.POST_NOT_FOUND
			)
		);
	}

	/**
	 * 여러 조건(삭제 포함, 컨텐츠 노출하지 않을 유저, 검색 키워드 등) 을 반영하여 Posts 페이지 반환
	 * @param boardId 게시판 아이디
	 * @param includeDeleted 삭제 게시물 포함 여부
	 * @param blockedUserIds 차단 하여 노출하지 않을 userId Set
	 * @param keyword 검색 키워드
	 * @param pageable pageable
	 * @return 게시물 페이지
	 */
	public Page<Post> findPostsByBoardWithFilters(
		String boardId,
		boolean includeDeleted,
		Set<String> blockedUserIds,
		String keyword,
		Pageable pageable
	) {

		return postRepository.findPostsByBoardWithFilters(boardId, includeDeleted, blockedUserIds, keyword, pageable);
	}
}
