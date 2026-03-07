package net.causw.app.main.domain.community.post.service.v2;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.util.LikePostValidator;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostWriter;
import net.causw.app.main.domain.user.account.entity.user.User;

@ExtendWith(MockitoExtension.class)
public class LikePostServiceTest {

	@InjectMocks
	LikePostService likePostService;

	@Mock
	PostReader postReader;

	@Mock
	LikePostWriter likePostWriter;

	@Mock
	LikePostValidator likePostValidator;

	@Nested
	@DisplayName("게시글 좋아요 테스트")
	class LikePostTest {
		Post post;
		User writer;

		@BeforeEach
		void setUp() {
			writer = mock(User.class);
			post = mock(Post.class);

			given(post.getWriter()).willReturn(writer);
			given(writer.isDeleted()).willReturn(false);
			given(postReader.findById("post-id")).willReturn(post);
		}

		@DisplayName("게시글 좋아요 성공")
		@Test
		void likePost_shouldSucceed() {
			// given

			// when
			likePostService.likePost("user-id", "post-id");

			// then
			verify(likePostValidator, times(1)).validateForLike("user-id", "post-id");
			verify(likePostWriter, times(1)).saveLikePost("user-id", post);
		}

		@DisplayName("이미 좋아요를 누른 경우 예외 발생")
		@Test
		void likePost_shouldFail_whenAlreadyLiked() {
			// given

			doThrow(new RuntimeException())
				.when(likePostValidator).validateForLike("user-id", "post-id");

			// when & then
			assertThatThrownBy(() -> likePostService.likePost("user-id", "post-id"))
				.isInstanceOf(RuntimeException.class);

			verify(likePostWriter, never()).saveLikePost(anyString(), any(Post.class));
		}
	}

	@Nested
	@DisplayName("게시글 좋아요 취소 테스트")
	class CancelLikePostTest {
		Post post;
		User writer;

		@BeforeEach
		void setUp() {
			writer = mock(User.class);
			post = mock(Post.class);

			given(post.getWriter()).willReturn(writer);
			given(writer.isDeleted()).willReturn(false);
			given(postReader.findById("post-id")).willReturn(post);
		}

		@DisplayName("게시글 좋아요 취소 성공")
		@Test
		void cancelLikePost_shouldSucceed() {
			// when
			likePostService.cancelLikePost("user-id", "post-id");

			// then
			verify(likePostValidator, times(1)).validateForCancelLike("user-id", "post-id");
			verify(likePostWriter, times(1)).deleteLikePost("user-id", "post-id");
		}

		@DisplayName("좋아요를 누르지 않은 상태에서 취소 시 예외 발생")
		@Test
		void cancelLikePost_shouldFail_whenNotLiked() {
			// given
			doThrow(new RuntimeException())
				.when(likePostValidator).validateForCancelLike("user-id", "post-id");

			// when & then
			assertThatThrownBy(() -> likePostService.cancelLikePost("user-id", "post-id"))
				.isInstanceOf(RuntimeException.class);

			verify(likePostWriter, never()).deleteLikePost(anyString(), anyString());
		}
	}
}