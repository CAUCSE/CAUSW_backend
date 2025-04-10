package net.causw.application.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.board.FavoriteBoardRepository;
import net.causw.adapter.persistence.repository.notification.UserBoardSubscribeRepository;
import net.causw.adapter.persistence.repository.post.FavoritePostRepository;
import net.causw.adapter.persistence.repository.post.LikePostRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.PostsResponseDto;
import net.causw.application.pageable.PageableFactory;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.util.ObjectFixtures;
import net.causw.domain.model.util.StaticValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @InjectMocks
  PostService postService;

  @Mock
  PostRepository postRepository;

  @Mock
  LikePostRepository likePostRepository;

  @Mock
  BoardRepository boardRepository;

  @Mock
  FavoriteBoardRepository favoriteBoardRepository;

  @Mock
  FavoritePostRepository favoritePostRepository;

  @Mock
  UserBoardSubscribeRepository userBoardSubscribeRepository;

  @Mock
  PageableFactory pageableFactory;


  @Nested
  class SearchPostTest {

    static final String boardId = "boardId";
    static final String keyword = "keyword";
    static final int pageNum = 0;
    User user;
    Board board;
    Post post;
    Pageable pageable;

    @BeforeEach
    public void setUp() {
      user = ObjectFixtures.getUser();
      board = ObjectFixtures.getBoard();
      post = ObjectFixtures.getPost(user, board);
      pageable = PageRequest.of(pageNum, StaticValue.DEFAULT_PAGE_SIZE);

      user.setRoles(Set.of(Role.COMMON));

      given(boardRepository.findById(boardId)).willReturn(Optional.ofNullable(board));
    }

    private static Stream<Arguments> provideUnauthorizedUserCases() {
      return Stream.of(
          Arguments.of(Role.NONE, UserState.ACTIVE),
          Arguments.of(Role.COMMON, UserState.INACTIVE),
          Arguments.of(Role.COMMON, UserState.DROP),
          Arguments.of(Role.COMMON, UserState.DELETED)
      );
    }

    private static Stream<Arguments> provideSearchByKeywordSuccessCases() {
      return Stream.of(
          Arguments.of("제목에 키워드 포함", null),
          Arguments.of(null, "내용에 키워드 포함"),
          Arguments.of("제목에 키워드 포함", "내용에 키워드 포함")
      );
    }

    @DisplayName("인증되지 않은 사용자는 게시글 조회 불가")
    @ParameterizedTest
    @MethodSource("provideUnauthorizedUserCases")
    void testUnauthorizedUser(Role role, UserState state) {
      // given
      user.setRoles(Set.of(role));
      user.setState(state);

      // when & then
      assertThatThrownBy(() -> postService.searchPost(user, boardId, keyword, pageNum))
          .isInstanceOf(UnauthorizedException.class);
    }

    @DisplayName("삭제된 게시판의 게시글 조회 불가능")
    @Test
    void testSearchPostByDeletedBoard() {
      //given
      user.setRoles(Set.of(Role.COMMON));
      board.setIsDeleted(true);

      //when & then
      assertThatThrownBy(() ->
          postService.searchPost(user, boardId, keyword, pageNum))
          .isInstanceOf(BadRequestException.class)
          .extracting("errorCode")
          .isEqualTo(ErrorCode.TARGET_DELETED);
    }

    @DisplayName("제목 또는 내용에 키워드를 포함한 게시글 조회 성공")
    @ParameterizedTest
    @MethodSource("provideSearchByKeywordSuccessCases")
    void testSearchPostWithKeyword(String title, String content) {
      // given
      String keyword = "키워드";
      post.update(title, content, null, null);
      Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

      given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
      given(postRepository.findByBoardIdAndKeywordAndIsDeleted(
          eq(keyword), eq(boardId), eq(pageable), eq(false))).willReturn(postPage);

      // when
      BoardPostsResponseDto result = postService.searchPost(user, boardId, keyword, pageNum);

      // then
      Page<PostsResponseDto> searchedPagedPost = result.getPost();
      verifyPost(searchedPagedPost, false);

      PostsResponseDto firstPost = searchedPagedPost.getContent().get(0);
      Optional.ofNullable(firstPost.getTitle())
          .map(t -> assertThat(t).contains(keyword));
      Optional.ofNullable(firstPost.getContent())
          .map(c -> assertThat(c).contains(keyword));
    }

    @DisplayName("제목과 내용에 키워드 포함하지 않는 경우 게시글 조회 결과 없음")
    @Test
    void testSearchPostNoResult() {
      // given
      Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

      given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
      given(postRepository.findByBoardIdAndKeywordAndIsDeleted(
          eq(keyword), eq(boardId), eq(pageable), eq(false))).willReturn(emptyPage);

      // when
      BoardPostsResponseDto result = postService.searchPost(user, boardId, keyword, pageNum);

      // then
      Page<PostsResponseDto> searchedPagedPost = result.getPost();
      assertThat(searchedPagedPost).isNotNull();
      assertThat(searchedPagedPost.getTotalElements()).isZero();
    }

    @DisplayName("일반 사용자는 삭제된 게시글 조회 불가능")
    @Test
    void testSearchPostByCommonUser() {
      //given
      Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

      given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
      given(postRepository.findByBoardIdAndKeywordAndIsDeleted(
          eq(keyword), eq(boardId), eq(pageable), eq(false))).willReturn(postPage);

      //when
      BoardPostsResponseDto result = postService.searchPost(user, boardId, keyword, pageNum);

      //then
      Page<PostsResponseDto> searchedPagedPost = result.getPost();
      verifyPost(searchedPagedPost, false);
    }

    @DisplayName("관리자는 삭제된 게시글 조회 가능")
    @Test
    void testSearchPostByAdmin() {
      //given
      user.setRoles(Set.of(Role.ADMIN));
      post.setIsDeleted(true);
      Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

      given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
      given(postRepository.findByBoardIdAndKeyword(
          eq(keyword), eq(boardId), eq(pageable))).willReturn(postPage);

      //when
      BoardPostsResponseDto result = postService.searchPost(user, boardId, keyword, pageNum);

      //then
      Page<PostsResponseDto> searchedPagedPost = result.getPost();
      verifyPost(searchedPagedPost, true);
    }

    private void verifyPost(Page<PostsResponseDto> searchedPagedPost, boolean isDeleted) {
      assertThat(searchedPagedPost).isNotNull();
      assertThat(searchedPagedPost.getTotalElements()).isEqualTo(1);
      assertThat(searchedPagedPost.getTotalPages()).isEqualTo(1);

      searchedPagedPost.getContent().forEach(
          postResponseDto -> {
            assertThat(postResponseDto).isNotNull();
            assertThat(postResponseDto.getIsDeleted()).isEqualTo(isDeleted);

            assertThat(postResponseDto.getTitle()).isEqualTo(post.getTitle());
            assertThat(postResponseDto.getContent()).isEqualTo(post.getContent());
            assertThat(postResponseDto.getWriterNickname()).isEqualTo(user.getNickname());
          });
    }
  }
}