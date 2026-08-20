package net.causw.app.main.domain.community.comment.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import net.causw.app.main.domain.community.comment.entity.CommentAnonymousNickname;
import net.causw.app.main.domain.community.comment.repository.CommentAnonymousNicknameRepository;
import net.causw.app.main.domain.community.comment.util.AnonymousNicknameGenerator;

@ExtendWith(MockitoExtension.class)
class CommentAnonymousNicknameResolverTest {

	@InjectMocks
	private CommentAnonymousNicknameResolver resolver;

	@Mock
	private CommentAnonymousNicknameRepository commentAnonymousNicknameRepository;

	@Mock
	private CommentAnonymousNicknameWriter commentAnonymousNicknameWriter;

	@Mock
	private AnonymousNicknameGenerator anonymousNicknameGenerator;

	@Test
	@DisplayName("같은 게시글에서 같은 유저가 다시 요청하면 기존 닉네임을 재사용하고 추가로 저장하지 않는다")
	void givenExistingMapping_whenResolve_thenReuseWithoutSaving() {
		// given
		String postId = "post-1";
		String userId = "user-1";
		CommentAnonymousNickname mine = CommentAnonymousNickname.of(postId, userId, "다정한 튜링 42");
		given(commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId))
			.willReturn(Optional.of(mine));

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isEqualTo("다정한 튜링 42");
		then(commentAnonymousNicknameWriter).should(never()).save(any(), any(), any());
	}

	@Test
	@DisplayName("게시글에 처음 요청하는 유저는 새 닉네임을 발급받고 저장한다")
	void givenNoExistingMapping_whenResolve_thenGenerateAndSaveNickname() {
		// given
		String postId = "post-1";
		String userId = "user-2";
		given(commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId))
			.willReturn(Optional.empty());
		given(anonymousNicknameGenerator.generate()).willReturn("성실한 호퍼 7");

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isEqualTo("성실한 호퍼 7");
		then(commentAnonymousNicknameWriter).should().save(postId, userId, "성실한 호퍼 7");
	}

	@Test
	@DisplayName("동시 요청으로 같은 유저의 매핑이 먼저 저장되면(post_id, user_id 충돌) 그 매핑을 재사용한다")
	void givenConcurrentSameUserInsert_whenResolve_thenReuseWinningNickname() {
		// given
		String postId = "post-1";
		String userId = "user-2";
		CommentAnonymousNickname winning = CommentAnonymousNickname.of(postId, userId, "성실한 호퍼 7");
		given(commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId))
			.willReturn(Optional.empty())
			.willReturn(Optional.of(winning));
		given(anonymousNicknameGenerator.generate()).willReturn("성실한 호퍼 7");
		doThrow(new DataIntegrityViolationException("unique constraint violated (post_id, user_id)"))
			.when(commentAnonymousNicknameWriter).save(postId, userId, "성실한 호퍼 7");

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isEqualTo("성실한 호퍼 7");
		then(commentAnonymousNicknameWriter).should(times(1)).save(any(), any(), any());
	}

	@Test
	@DisplayName("다른 유저가 같은 닉네임 후보를 먼저 선점하면(post_id, nickname 충돌) 새 후보로 재시도한다")
	void givenNicknameCollisionWithDifferentUser_whenResolve_thenRetryWithNewCandidate() {
		// given
		String postId = "post-1";
		String userId = "user-2";
		// 충돌 원인이 나 자신과의 경합이 아니므로, 재조회해도 내 매핑은 계속 없다
		given(commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId))
			.willReturn(Optional.empty());
		given(anonymousNicknameGenerator.generate())
			.willReturn("성실한 호퍼 7", "용감한 잡스 3");
		doThrow(new DataIntegrityViolationException("unique constraint violated (post_id, nickname)"))
			.doNothing()
			.when(commentAnonymousNicknameWriter).save(eq(postId), eq(userId), any());

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isEqualTo("용감한 잡스 3");
		then(commentAnonymousNicknameWriter).should(times(2)).save(any(), any(), any());
	}

	@Test
	@DisplayName("랜덤 닉네임 후보가 연속으로 충돌하면 결정적 폴백 닉네임으로 전환한다")
	void givenRepeatedRandomCollisions_whenResolve_thenFallBackToDeterministicNickname() {
		// given
		String postId = "post-1";
		String userId = "user-2";
		given(commentAnonymousNicknameRepository.findByPostIdAndUserId(postId, userId))
			.willReturn(Optional.empty());
		given(anonymousNicknameGenerator.generate()).willReturn("성실한 호퍼 7");
		given(commentAnonymousNicknameRepository.countByPostId(postId)).willReturn(3L);
		doThrow(new DataIntegrityViolationException("collision"))
			.doThrow(new DataIntegrityViolationException("collision"))
			.doThrow(new DataIntegrityViolationException("collision"))
			.doThrow(new DataIntegrityViolationException("collision"))
			.doThrow(new DataIntegrityViolationException("collision"))
			.doNothing()
			.when(commentAnonymousNicknameWriter).save(eq(postId), eq(userId), any());

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isEqualTo("행복한 잡스4");
		then(commentAnonymousNicknameWriter).should(times(6)).save(any(), any(), any());
	}
}
