package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.CommentAnonymousNickname;
import net.causw.app.main.domain.community.comment.repository.CommentAnonymousNicknameRepository;
import net.causw.app.main.domain.community.comment.util.AnonymousNicknameGenerator;

import lombok.RequiredArgsConstructor;

/**
 * 게시글 안에서 익명 댓글 작성자에게 "수식어+명사+숫자" 랜덤 닉네임을 부여하고,
 * 동일 작성자가 같은 게시글에 다시 댓글을 달면 기존 닉네임을 재사용한다.
 *
 * <p>(post_id, user_id) 단건 조회로 재사용 여부를 판단하므로 게시글의 익명 작성자 수와
 * 무관하게 조회 비용이 일정하다. 신규 발급은 {@link CommentAnonymousNicknameWriter}를 통해
 * 별도 트랜잭션에서 즉시 flush하여 유니크 제약 충돌을 그 자리에서 감지하고, 충돌 시
 * (post_id, user_id)를 재조회해 원인을 구분한다 — 동시 요청으로 내 매핑이 먼저 저장된
 * 경우(자기 자신과의 충돌)라면 그 매핑을 재사용하고, 다른 사용자가 같은 닉네임 후보를
 * 먼저 선점한 경우(post_id, nickname 충돌)라면 여전히 내 매핑이 없으므로 새 후보로
 * 재시도한다.</p>
 */
@Component
@RequiredArgsConstructor
public class CommentAnonymousNicknameResolver {

	/** 조합 공간(수식어 100 × 명사 32 × 숫자 100 ≈ 32만) 대비 충분히 넉넉한 재시도 상한. */
	private static final int MAX_INSERT_ATTEMPTS = 20;

	private final CommentAnonymousNicknameRepository commentAnonymousNicknameRepository;
	private final CommentAnonymousNicknameWriter commentAnonymousNicknameWriter;
	private final AnonymousNicknameGenerator anonymousNicknameGenerator;

	public String resolve(String postId, String userId) {
		return commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId)
			.map(CommentAnonymousNickname::getNickname)
			.orElseGet(() -> insertUnusedNickname(postId, userId));
	}

	private String insertUnusedNickname(String postId, String userId) {
		DataIntegrityViolationException lastFailure = null;

		for (int attempt = 0; attempt < MAX_INSERT_ATTEMPTS; attempt++) {
			String nickname = anonymousNicknameGenerator.generate();
			try {
				commentAnonymousNicknameWriter.save(postId, userId, nickname);
				return nickname;
			} catch (DataIntegrityViolationException e) {
				lastFailure = e;
				Optional<String> mine = commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId)
					.map(CommentAnonymousNickname::getNickname);
				if (mine.isPresent()) {
					return mine.get();
				}
			}
		}

		throw new IllegalStateException(
			"게시글의 익명 닉네임 발급에 반복적으로 실패했습니다. postId=" + postId + ", userId=" + userId
				+ ", attempts=" + MAX_INSERT_ATTEMPTS,
			lastFailure);
	}
}
