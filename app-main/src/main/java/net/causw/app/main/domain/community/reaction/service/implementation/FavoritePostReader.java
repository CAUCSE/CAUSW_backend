package net.causw.app.main.domain.community.reaction.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.reaction.repository.FavoritePostRepository;

import lombok.RequiredArgsConstructor;

/**
 * FavoritePost 도메인의 읽기 전용 작업을 담당하는 컴포넌트
 * Repository에서 데이터를 조회합니다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoritePostReader {
	private final FavoritePostRepository favoritePostRepository;

	/**
	 * 특정 게시글의 즐겨찾기 개수를 조회합니다.
	 *
	 * @param postId 게시글 ID
	 * @return 즐겨찾기 개수
	 */
	public Long countByPostId(String postId) {
		return favoritePostRepository.countByPostId(postId);
	}

	/**
	 * 특정 사용자가 특정 게시글을 즐겨찾기했는지 확인합니다.
	 *
	 * @param postId 게시글 ID
	 * @param userId 사용자 ID
	 * @return 즐겨찾기 여부
	 */
	public Boolean existsByPostIdAndUserId(String postId, String userId) {
		return favoritePostRepository.existsByPostIdAndUserId(postId, userId);
	}
}
