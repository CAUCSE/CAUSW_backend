package net.causw.app.main.domain.community.comment.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import net.causw.app.main.domain.community.comment.entity.CommentAnonymousNickname;
import net.causw.app.main.domain.community.comment.repository.CommentAnonymousNicknameRepository;

@ExtendWith(MockitoExtension.class)
class CommentAnonymousNicknameResolverTest {

	@InjectMocks
	private CommentAnonymousNicknameResolver resolver;

	@Mock
	private CommentAnonymousNicknameRepository commentAnonymousNicknameRepository;

	@Test
	@DisplayName("같은 게시글에서 같은 유저가 다시 요청하면 기존 닉네임을 재사용하고 추가로 저장하지 않는다")
	void givenExistingMapping_whenResolve_thenReuseWithoutSaving() {
		// given
		String postId = "post-1";
		String userId = "user-1";
		CommentAnonymousNickname mine = CommentAnonymousNickname.of(postId, userId, "다정함 42 튜링");
		given(commentAnonymousNicknameRepository.findAllByPostId(postId)).willReturn(List.of(mine));

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isEqualTo("다정함 42 튜링");
		then(commentAnonymousNicknameRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("게시글에 처음 요청하는 유저는 기존에 쓰이지 않은 닉네임을 새로 발급받고 저장한다")
	void givenNoExistingMapping_whenResolve_thenGenerateAndSaveUnusedNickname() {
		// given
		String postId = "post-1";
		String userId = "user-2";
		CommentAnonymousNickname other = CommentAnonymousNickname.of(postId, "user-1", "다정함 42 튜링");
		given(commentAnonymousNicknameRepository.findAllByPostId(postId)).willReturn(List.of(other));

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isNotEqualTo("다정함 42 튜링");
		ArgumentCaptor<CommentAnonymousNickname> captor = ArgumentCaptor.forClass(CommentAnonymousNickname.class);
		then(commentAnonymousNicknameRepository).should().save(captor.capture());
		assertThat(captor.getValue().getPostId()).isEqualTo(postId);
		assertThat(captor.getValue().getUserId()).isEqualTo(userId);
		assertThat(captor.getValue().getNickname()).isEqualTo(nickname);
	}

	@Test
	@DisplayName("저장 시 동시성으로 유니크 제약이 충돌하면 재조회해서 상대방이 먼저 저장한 닉네임을 반환한다")
	void givenConcurrentInsertConflict_whenResolve_thenReReadWinningNickname() {
		// given
		String postId = "post-1";
		String userId = "user-2";
		given(commentAnonymousNicknameRepository.findAllByPostId(postId))
			.willReturn(List.of())
			.willReturn(List.of(CommentAnonymousNickname.of(postId, userId, "성실함 7 호퍼")));
		given(commentAnonymousNicknameRepository.save(any()))
			.willThrow(new DataIntegrityViolationException("unique constraint violated"));

		// when
		String nickname = resolver.resolve(postId, userId);

		// then
		assertThat(nickname).isEqualTo("성실함 7 호퍼");
	}
}
