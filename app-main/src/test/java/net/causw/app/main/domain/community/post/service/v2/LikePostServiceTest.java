package net.causw.app.main.domain.community.post.service.v2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

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
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;

@ExtendWith(MockitoExtension.class)
public class LikePostServiceTest {

	@InjectMocks
	PostService postService;

	@Mock
	PostReader postReader;

	@Mock
	LikePostReader likePostReader;

	@Mock
	LikePostWriter likePostWriter;

	@Nested
	@DisplayName("게시글 좋아요 테스트")
	class LikePostTest {
		Post post;
		User writer;

		@BeforeEach
		void setUp() {
			writer = mock(User.class);
			post = mock(Post.class);

			// PostValidator.validateWriterNotDeleted 통과를 위한 셋팅
			given(post.getWriter()).willReturn(writer);
			given(writer.getState()).willReturn(UserState.ACTIVE); // 실제 프로젝트의 활성 상태값에 맞게 조정
			given(postReader.findById("post-id")).willReturn(post);
		}

		@DisplayName("게시글 좋아요 성공")
		@Test
		void likePost_shouldSucceed() {
			// given
			given(likePostReader.existsByPostIdAndUserId("user-id", "post-id")).willReturn(false);

			// when
			postService.likePost("user-id", "post-id");

			// then
			verify(likePostWriter, times(1)).saveLikePost("user-id", post);
		}

		@DisplayName("이미 좋아요를 누른 경우 예외 발생")
		@Test
		void likePost_shouldFail_whenAlreadyLiked() {
			// given
			given(likePostReader.existsByPostIdAndUserId("user-id", "post-id")).willReturn(true);

			// when & then
			assertThatThrownBy(() -> postService.likePost("user-id", "post-id"))
				// LikePostErrorCode.POST_ALREADY_LIKED.toBaseException()의 실제 반환 타입으로 변경 권장 (예: BaseRunTimeV2Exception.class)
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

			// PostValidator.validateWriterNotDeleted 통과를 위한 셋팅
			given(post.getWriter()).willReturn(writer);
			given(writer.getState()).willReturn(UserState.ACTIVE);
			given(postReader.findById("post-id")).willReturn(post);
		}

		@DisplayName("게시글 좋아요 취소 성공")
		@Test
		void cancelLikePost_shouldSucceed() {
			// given
			given(likePostReader.existsByPostIdAndUserId("user-id", "post-id")).willReturn(true);

			// when
			postService.cancelLikePost("user-id", "post-id");

			// then
			verify(likePostWriter, times(1)).deleteLikePost("user-id", "post-id");
		}

		@DisplayName("좋아요를 누르지 않은 상태에서 취소 시 예외 발생")
		@Test
		void cancelLikePost_shouldFail_whenNotLiked() {
			// given
			given(likePostReader.existsByPostIdAndUserId("user-id", "post-id")).willReturn(false);

			// when & then
			assertThatThrownBy(() -> postService.cancelLikePost("user-id", "post-id"))
				.isInstanceOf(RuntimeException.class);

			verify(likePostWriter, never()).deleteLikePost(anyString(), anyString());
		}
	}
}