package net.causw.app.main.domain.community.post.service.v1;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Post 엔티티 조회 서비스
 * 게시글 엔티티에 대한 기본 조회 작업을 담당
 */
@Service
@RequiredArgsConstructor
public class PostEntityService {
	// 게시글 저장소
	private final PostRepository postRepository;

	/**
	 * 삭제되지 않은 게시글을 ID로 조회
	 * @param postId 조회할 게시글 ID
	 * @return 조회된 게시글 엔티티
	 * @throws NotFoundException 게시글이 존재하지 않거나 삭제된 경우
	 */
	public Post findByIdNotDeleted(String postId) {

		return postRepository.findByIdAndIsDeletedFalse(postId).orElseThrow(() -> new NotFoundException(
			ErrorCode.ROW_DOES_NOT_EXIST,
			MessageUtil.POST_NOT_FOUND));
	}

	/**
	 * 여러 조건(삭제 포함, 컨텐츠 노출하지 않을 유저, 검색 키워드 등) 을 반영하여 Posts 페이지 반환
	 * @param boardId 게시판 아이디
	 * @param includeDeleted 삭제 게시물 포함 여부 (관리자/동아리장은 true)
	 * @param blockedUserIds 차단 하여 노출하지 않을 userId Set (차단한 유저의 게시글 제외)
	 * @param keyword 검색 키워드 (제목, 내용 검색)
	 * @param pageable pageable (페이지 번호, 페이지 크기, 정렬 정보)
	 * @return 게시물 페이지 (필터링된 게시글 목록)
	 */
	public Page<Post> findPostsByBoardWithFilters(
		String boardId,
		boolean includeDeleted,
		Set<String> blockedUserIds,
		String keyword,
		Pageable pageable) {
		return postRepository.findPostsByBoardWithFilters(boardId, includeDeleted, blockedUserIds, keyword, pageable);
	}
}
