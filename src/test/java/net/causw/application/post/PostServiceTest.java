package net.causw.application.post;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import net.causw.adapter.persistence.post.LikePost;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.post.LikePostRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.util.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

  @InjectMocks
  PostService postService;

  @Mock
  PostRepository postRepository;

  @Mock
  LikePostRepository likePostRepository;


  @Nested
  @DisplayName("게시글 좋아요 테스트")
  class PostLikeTest{

    User user;
    Post post;
    User writer;

    @BeforeEach
    void setUp() {
      user = mock(User.class);
      post = mock(Post.class);
      writer = mock(User.class);
    }

    @DisplayName("좋아요 성공 테스트")
    @Test
    void likePost_shouldSucceed_whenPostIsNotLiked() {
      // given
      String postId = "dummy123";

      UserState userStateNotDeleted = UserState.ACTIVE;
      given(post.getWriter()).willReturn(writer);
      given(writer.getState()).willReturn(userStateNotDeleted);

      when(postRepository.findById(postId)).thenReturn(Optional.of(post));
      when(likePostRepository.existsByPostIdAndUserId(postId, user.getId())).thenReturn(false);

      // When
      postService.likePost(user, postId);

      // Then
      verify(likePostRepository, times(1)).save(any(LikePost.class));
    }

    @DisplayName("게시물 작성 유저 삭제 상태일시 좋아요 실패")
    @Test
    void likePost_shouldFail_whenWriterIsDeleted() {
      // given
      String postId = "dummy123";

      UserState userStateDeleted = UserState.DELETED;
      given(post.getWriter()).willReturn(writer);
      given(writer.getState()).willReturn(userStateDeleted);

      when(postRepository.findById(postId)).thenReturn(Optional.of(post));

      // When & Then
      assertThatThrownBy(() -> postService.likePost(user, postId))
          .isInstanceOf(UnauthorizedException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.DELETED_USER);

      verify(likePostRepository, times(0)).save(any(LikePost.class));
    }

    @DisplayName("좋아요 이미 한 게시물에 대해서 좋아요 실패")
    @Test
    void likePost_shouldFail_whenAlreadyLiked() {
      // given
      String postId = "dummy123";

      UserState userStateNotDeleted = UserState.ACTIVE;
      given(post.getWriter()).willReturn(writer);
      given(writer.getState()).willReturn(userStateNotDeleted);

      when(postRepository.findById(postId)).thenReturn(Optional.of(post));
      when(likePostRepository.existsByPostIdAndUserId(postId, user.getId())).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> postService.likePost(user, postId))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining(MessageUtil.POST_ALREADY_LIKED)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.ROW_ALREADY_EXIST);

      verify(likePostRepository, times(0)).save(any(LikePost.class));
    }
  }

  @Nested
  @DisplayName("게시글 좋아요 취소 테스트")
  class PostCancelLikeTest{

    User user;
    Post post;
    User writer;

    @BeforeEach
    void setUp() {
      user = mock(User.class);
      post = mock(Post.class);
      writer = mock(User.class);
    }

    @DisplayName("좋아요 취소 성공 테스트")
    @Test
    void cancelLikePost_shouldSucceed_whenPostIsLiked() {
      // given
      String postId = "dummy123";
      String userId = "dummy1234";

      UserState userStateNotDeleted = UserState.ACTIVE;
      given(post.getWriter()).willReturn(writer);
      given(writer.getState()).willReturn(userStateNotDeleted);
      given(user.getId()).willReturn(userId);

      when(postRepository.findById(postId)).thenReturn(Optional.of(post));
      when(likePostRepository.existsByPostIdAndUserId(postId, user.getId())).thenReturn(true);

      // When
      postService.cancelLikePost(user, postId);

      // Then
      verify(likePostRepository, times(1)).deleteLikeByPostIdAndUserId(postId, user.getId());
    }

    @DisplayName("게시물 작성 유저 삭제 상태일시 좋아요 취소 실패")
    @Test
    void cancelLikePost_shouldFail_whenWriterIsDeleted() {
      // given
      String postId = "dummy123";

      UserState userStateDeleted = UserState.DELETED;
      given(post.getWriter()).willReturn(writer);
      given(writer.getState()).willReturn(userStateDeleted);

      when(postRepository.findById(postId)).thenReturn(Optional.of(post));

      // When & Then
      assertThatThrownBy(() -> postService.cancelLikePost(user, postId))
          .isInstanceOf(UnauthorizedException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.DELETED_USER);

      verify(likePostRepository, times(0)).deleteLikeByPostIdAndUserId(postId, user.getId());
    }

    @DisplayName("좋아요 하지 않은 게시물에 대해서 좋아요 취소 실패")
    @Test
    void cancelLikePost_shouldFail_whenPostIsNotLiked() {
      // given
      String postId = "dummy123";

      UserState userStateNotDeleted = UserState.ACTIVE;
      given(post.getWriter()).willReturn(writer);
      given(writer.getState()).willReturn(userStateNotDeleted);

      when(postRepository.findById(postId)).thenReturn(Optional.of(post));
      when(likePostRepository.existsByPostIdAndUserId(postId, user.getId())).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> postService.cancelLikePost(user, postId))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining(MessageUtil.POST_NOT_LIKED)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.ROW_DOES_NOT_EXIST);

      verify(likePostRepository, times(0)).deleteLikeByPostIdAndUserId(postId, user.getId());
    }
  }

}
