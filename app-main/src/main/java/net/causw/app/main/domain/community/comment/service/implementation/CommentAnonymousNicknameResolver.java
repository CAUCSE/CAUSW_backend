package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.CommentAnonymousNickname;
import net.causw.app.main.domain.community.comment.repository.CommentAnonymousNicknameRepository;
import net.causw.app.main.domain.community.comment.util.AnonymousNicknameGenerator;

import lombok.RequiredArgsConstructor;

/**
 * 게시글 안에서 익명 댓글 작성자에게 "능력치+숫자+명사" 랜덤 닉네임을 부여하고,
 * 동일 작성자가 같은 게시글에 다시 댓글을 달면 기존 닉네임을 재사용한다.
 *
 * <p>게시글당 매핑을 한 번에 조회해 중복 체크와 재사용 판단을 동시에 끝내므로,
 * 정상적인 경우 조회 1회(+ 최초 1건일 때만 저장 1회)만 발생한다.</p>
 */
@Component
@RequiredArgsConstructor
public class CommentAnonymousNicknameResolver {

	private final CommentAnonymousNicknameRepository commentAnonymousNicknameRepository;

	public String resolve(String postId, String userId) {
		List<CommentAnonymousNickname> existing = commentAnonymousNicknameRepository.findAllByPostId(postId);

		Optional<String> mine = findNicknameOf(existing, userId);
		if (mine.isPresent()) {
			return mine.get();
		}

		Set<String> used = existing.stream()
			.map(CommentAnonymousNickname::getNickname)
			.collect(Collectors.toSet());
		String nickname = AnonymousNicknameGenerator.generateUnused(used);

		try {
			commentAnonymousNicknameRepository.save(CommentAnonymousNickname.of(postId, userId, nickname));
			return nickname;
		} catch (DataIntegrityViolationException e) {
			// 동시 요청으로 유니크 제약(post_id, user_id)이 충돌한 경우에만 재조회해 재사용
			return findNicknameOf(commentAnonymousNicknameRepository.findAllByPostId(postId), userId)
				.orElseThrow(() -> e);
		}
	}

	private Optional<String> findNicknameOf(List<CommentAnonymousNickname> mappings, String userId) {
		return mappings.stream()
			.filter(mapping -> mapping.getUserId().equals(userId))
			.map(CommentAnonymousNickname::getNickname)
			.findFirst();
	}
}
