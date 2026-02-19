package net.causw.app.main.domain.community.reaction.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.reaction.repository.LikePostRepository;

import lombok.RequiredArgsConstructor;

/**
 * LikePost 도메인의 읽기 전용 작업을 담당하는 컴포넌트
 * Repository에서 데이터를 조회합니다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikePostReader {
	private final LikePostRepository likePostRepository;

	/**
	 * 특정 게시글의 좋아요 개수를 조회합니다.
	 *
	 * @param postId 게시글 ID
	 * @return 좋아요 개수
	 */
	public Long countByPostId(String postId) {
		return likePostRepository.countByPostId(postId);
	}

	/**
	 * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.
	 *
	 * @param postId 게시글 ID
	 * @param userId 사용자 ID
	 * @return 좋아요 여부
	 */
	public Boolean existsByPostIdAndUserId(String postId, String userId) {
		return likePostRepository.existsByPostIdAndUserId(postId, userId);
	}
}
