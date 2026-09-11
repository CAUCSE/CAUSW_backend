package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.repository.CommentAnonymousNicknameRepository;
import net.causw.app.main.domain.community.common.util.AnonymousNicknameGenerator;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시글 안에서 익명 댓글 작성자에게 "수식어+명사+숫자" 랜덤 닉네임과 기본 제공 4종 중 랜덤
 * 프로필 이미지를 부여하고, 동일 작성자가 같은 게시글에 다시 댓글을 달면 기존 값을 재사용한다.
 *
 * <p>(post_id, user_id) 단건 조회로 재사용 여부를 판단하므로 게시글의 익명 작성자 수와
 * 무관하게 조회 비용이 일정하다. 신규 발급은 {@link CommentAnonymousNicknameWriter}를 통해
 * 별도 트랜잭션에서 즉시 flush하여 유니크 제약 충돌을 그 자리에서 감지하고, 충돌 시
 * (post_id, user_id)를 재조회해 원인을 구분한다 — 동시 요청으로 내 매핑이 먼저 저장된
 * 경우(자기 자신과의 충돌)라면 그 매핑을 재사용하고, 다른 사용자가 같은 닉네임 후보를
 * 먼저 선점한 경우(post_id, nickname 충돌)라면 여전히 내 매핑이 없으므로 새 후보로
 * 재시도한다. 프로필 이미지 타입은 유일성 제약이 없으므로 재시도 여부와 무관하게 매번
 * 새로 뽑아 닉네임과 함께 저장한다.</p>
 *
 * <p>랜덤 후보가 {@link #MAX_RANDOM_ATTEMPTS}번 연속 충돌하는 극단적인 경우에도 댓글 작성이
 * 실패해서는 안 되므로, 그 이후에는 무작위 생성 풀과 겹치지 않는 고정 수식어+명사에
 * 게시글 내 발급 순번을 붙인 결정적 폴백 닉네임(예: "행복한 잡스1")으로 전환해 계속 발급을
 * 시도한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentAnonymousNicknameResolver {

	/** 랜덤 후보 재시도 상한. 조합 공간(수식어 100 × 명사 43 × 숫자 100 = 430,000) 대비
	 *  이 안에서 거의 항상 해결되며, 넘어가면 결정적 폴백으로 전환한다. */
	private static final int MAX_RANDOM_ATTEMPTS = 5;

	/** 폴백 닉네임끼리도 동시 발급으로 우연히 겹칠 경우를 대비한 재시도 상한. */
	private static final int MAX_FALLBACK_ATTEMPTS = 5;

	/** 무작위 생성 풀(TRAITS)에 없는 수식어를 써서 랜덤 후보와 절대 겹치지 않는 폴백 접두어. */
	private static final String FALLBACK_NICKNAME_PREFIX = "행복한 잡스";

	private final CommentAnonymousNicknameRepository commentAnonymousNicknameRepository;
	private final CommentAnonymousNicknameWriter commentAnonymousNicknameWriter;
	private final AnonymousNicknameGenerator anonymousNicknameGenerator;

	/**
	 * 게시글(postId) 안에서 작성자(userId)가 사용할 익명 닉네임+프로필이미지를 확정해 반환한다.
	 *
	 * <p>같은 게시글에 같은 작성자가 이미 익명 댓글을 단 적이 있으면 그때 배정된 값을
	 * 그대로 재사용하고, 처음이면 새로 발급한다.</p>
	 *
	 * @param postId 댓글이 달리는 게시글 ID (재사용 스코프의 기준)
	 * @param userId 댓글 작성자 ID
	 * @return 이번 요청에서 사용할 (닉네임, 프로필이미지타입) 조합
	 */
	public CommentAnonymousIdentity resolve(String postId, String userId) {
		// (post_id, user_id) 조합으로 이미 발급된 매핑이 있으면 재사용하고, 없을 때만 새로 발급한다.
		return commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId)
			.map(entity -> new CommentAnonymousIdentity(entity.getNickname(), entity.getProfileImageType()))
			.orElseGet(() -> insertUnusedIdentity(postId, userId));
	}

	private CommentAnonymousIdentity insertUnusedIdentity(String postId, String userId) {
		DataIntegrityViolationException[] lastFailure = new DataIntegrityViolationException[1];

		for (int attempt = 0; attempt < MAX_RANDOM_ATTEMPTS; attempt++) {
			CommentAnonymousIdentity candidate = new CommentAnonymousIdentity(
				anonymousNicknameGenerator.generate(), anonymousNicknameGenerator.generateProfileImageType());
			Optional<CommentAnonymousIdentity> saved = trySave(postId, userId, candidate, attempt, lastFailure);
			if (saved.isPresent()) {
				return saved.get();
			}
		}

		log.warn("랜덤 닉네임 후보가 {}회 연속 충돌해 결정적 폴백 닉네임으로 전환합니다. postId={}, userId={}",
			MAX_RANDOM_ATTEMPTS, postId, userId);
		return insertFallbackIdentity(postId, userId, lastFailure);
	}

	private CommentAnonymousIdentity insertFallbackIdentity(
		String postId, String userId, DataIntegrityViolationException[] lastFailure) {
		long startingNumber = commentAnonymousNicknameRepository.countByPostId(postId) + 1;

		for (int attempt = 0; attempt < MAX_FALLBACK_ATTEMPTS; attempt++) {
			CommentAnonymousIdentity candidate = new CommentAnonymousIdentity(
				FALLBACK_NICKNAME_PREFIX + (startingNumber + attempt),
				anonymousNicknameGenerator.generateProfileImageType());
			Optional<CommentAnonymousIdentity> saved = trySave(postId, userId, candidate, attempt, lastFailure);
			if (saved.isPresent()) {
				return saved.get();
			}
		}

		String message = "게시글의 익명 닉네임 발급에 반복적으로 실패했습니다. postId=" + postId + ", userId=" + userId;
		log.error(message);
		throw CommentErrorCode.ANONYMOUS_NICKNAME_ISSUE_FAILED.toBaseException(message, lastFailure[0]);
	}

	/**
	 * 닉네임+프로필이미지 저장을 시도한다. 유니크 제약 충돌 시 (post_id, user_id)를 재조회해,
	 * 동시 요청으로 내 매핑이 이미 저장돼 있으면 그 값을 반환하고,
	 * 아니라면(다른 사용자와의 닉네임 후보 중복) 빈 값을 반환해 호출부가 다음 후보로 재시도하게 한다.
	 */
	private Optional<CommentAnonymousIdentity> trySave(
		String postId, String userId, CommentAnonymousIdentity candidate, int attempt,
		DataIntegrityViolationException[] lastFailure) {
		try {
			commentAnonymousNicknameWriter.save(postId, userId, candidate.nickname(), candidate.profileImageType());
			return Optional.of(candidate);
		} catch (DataIntegrityViolationException e) {
			lastFailure[0] = e;
			log.debug("익명 닉네임 후보가 충돌해 재시도합니다. postId={}, userId={}, attempt={}, candidate={}",
				postId, userId, attempt, candidate.nickname(), e);
			return commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId)
				.map(entity -> new CommentAnonymousIdentity(entity.getNickname(), entity.getProfileImageType()));
		}
	}
}
