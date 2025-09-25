package net.causw.app.main.service.post;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.post.LikePost;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.dto.post.BoardPostsResponseDto;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.repository.board.BoardRepository;
import net.causw.app.main.repository.board.FavoriteBoardRepository;
import net.causw.app.main.repository.notification.UserBoardSubscribeRepository;
import net.causw.app.main.repository.post.FavoritePostRepository;
import net.causw.app.main.repository.post.LikePostRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.post.query.PostQueryRepository;
import net.causw.app.main.repository.post.query.PostQueryResult;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.app.main.service.userBlock.UserBlockEntityService;
import net.causw.app.main.util.ObjectFixtures;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

	@InjectMocks
	PostService postService;

	@Mock
	PostRepository postRepository;

	@Mock
	PostQueryRepository postQueryRepository;

	@Mock
	LikePostRepository likePostRepository;

	@Mock
	BoardRepository boardRepository;

	@Mock
	PageableFactory pageableFactory;

	@Mock
	UserBlockEntityService userBlockEntityService;

	@Mock
	FavoriteBoardRepository favoriteBoardRepository;

	@Mock
	UserBoardSubscribeRepository userBoardSubscribeRepository;

	@Mock
	FavoritePostRepository favoritePostRepository;

	@Mock
	PostEntityService postEntityService;

	@Nested
	@DisplayName("게시글 좋아요 테스트")
	class PostLikeTest {

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
	class PostCancelLikeTest {

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

	@Nested
	class findAllPostTest {

		static final String boardId = "boardId";
		static final String keyword = "keyword";
		static final int pageNum = 0;
		User user;
		Board board;
		Post post;
		Pageable pageable;
		PostQueryResult postQueryResult;

		@BeforeEach
		public void setUp() {
			user = ObjectFixtures.getUser();
			board = ObjectFixtures.getBoard();
			post = ObjectFixtures.getPost(user, board);
			pageable = PageRequest.of(pageNum, StaticValue.DEFAULT_PAGE_SIZE);
			postQueryResult = new PostQueryResult(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				0L, 0L, 0L,
				post.getIsAnonymous(),
				post.getIsQuestion(),
				false, false,
				post.getIsDeleted(),
				true, // writer 존재 여부
				post.getWriter().getName(),
				post.getWriter().getNickname(),
				post.getWriter().getAdmissionYear(),
				post.getWriter().getState(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				null // 썸네일 url
			);

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
			assertThatThrownBy(() -> postService.findAllPost(user, boardId, keyword, pageNum))
				.isInstanceOf(UnauthorizedException.class);
		}

		@DisplayName("삭제된 게시판의 게시글 조회 불가능")
		@Test
		void testfindAllPostByDeletedBoard() {
			//given
			user.setRoles(Set.of(Role.COMMON));
			board.setIsDeleted(true);

			//when & then
			assertThatThrownBy(() ->
				postService.findAllPost(user, boardId, keyword, pageNum))
				.isInstanceOf(BadRequestException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.TARGET_DELETED);
		}

		@DisplayName("제목 또는 내용에 키워드를 포함한 게시글 조회 성공")
		@ParameterizedTest
		@MethodSource("provideSearchByKeywordSuccessCases")
		void testfindAllPostWithKeyword(String title, String content) {
			// given
			String keyword = "키워드";
			PostQueryResult postQueryResult = new PostQueryResult(
				post.getId(),
				title,
				content,
				0L, 0L, 0L,
				post.getIsAnonymous(),
				post.getIsQuestion(),
				false, false,
				post.getIsDeleted(),
				true, // writer 존재 여부
				post.getWriter().getName(),
				post.getWriter().getNickname(),
				post.getWriter().getAdmissionYear(),
				post.getWriter().getState(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				null // 썸네일 url
			);

			Page<PostQueryResult> postPage = new PageImpl<>(List.of(postQueryResult), pageable, 1);

			given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
			given(postQueryRepository.findPostsByBoardWithFilters(
				eq(boardId), eq(false), eq(Set.of()), eq(keyword), eq(pageable)))
				.willReturn(postPage);

			// when
			BoardPostsResponseDto result = postService.findAllPost(user, boardId, keyword, pageNum);

			// then
			Page<PostsResponseDto> searchedPagedPost = result.getPost();
			verifyPost(searchedPagedPost, false, title, content);

			PostsResponseDto firstPost = searchedPagedPost.getContent().get(0);
			Optional.ofNullable(firstPost.getTitle())
				.map(t -> assertThat(t).contains(keyword));
			Optional.ofNullable(firstPost.getContent())
				.map(c -> assertThat(c).contains(keyword));
		}

		@DisplayName("제목과 내용에 키워드 포함하지 않는 경우 게시글 조회 결과 없음")
		@Test
		void testfindAllPostNoResult() {
			// given
			Page<PostQueryResult> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

			given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
			given(postQueryRepository.findPostsByBoardWithFilters(
				eq(boardId), eq(false), eq(Set.of()), eq(keyword), eq(pageable)
			)).willReturn(emptyPage);

			// when
			BoardPostsResponseDto result = postService.findAllPost(user, boardId, keyword, pageNum);

			// then
			Page<PostsResponseDto> searchedPagedPost = result.getPost();
			assertThat(searchedPagedPost).isNotNull();
			assertThat(searchedPagedPost.getTotalElements()).isZero();
		}

		@DisplayName("일반 사용자는 삭제된 게시글 조회 불가능")
		@Test
		void testfindAllPostByCommonUser() {
			//given
			Page<PostQueryResult> postPage = new PageImpl<>(List.of(postQueryResult), pageable, 1);

			given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
			given(postQueryRepository.findPostsByBoardWithFilters(eq(boardId), eq(false), eq(Set.of()), eq(keyword),
				eq(pageable))).willReturn(postPage);

			//when
			BoardPostsResponseDto result = postService.findAllPost(user, boardId, keyword, pageNum);

			//then
			Page<PostsResponseDto> searchedPagedPost = result.getPost();
			verifyPost(searchedPagedPost, false, post.getTitle(), post.getContent());
		}

		@DisplayName("관리자는 삭제된 게시글 조회 가능")
		@Test
		void testfindAllPostByAdmin() {
			//given
			user.setRoles(Set.of(Role.ADMIN));
			post.setIsDeleted(true);
			PostQueryResult postQueryResult = new PostQueryResult(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				0L, 0L, 0L,
				post.getIsAnonymous(),
				post.getIsQuestion(),
				false, false,
				post.getIsDeleted(),
				true, // writer 존재 여부
				post.getWriter().getName(),
				post.getWriter().getNickname(),
				post.getWriter().getAdmissionYear(),
				post.getWriter().getState(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				null // 썸네일 url
			);
			Page<PostQueryResult> postPage = new PageImpl<>(List.of(postQueryResult), pageable, 1);

			given(pageableFactory.create(anyInt(), anyInt())).willReturn(pageable);
			given(postQueryRepository.findPostsByBoardWithFilters(
				eq(boardId), eq(true), eq(Set.of()), eq(keyword), eq(pageable))).willReturn(postPage);

			//when
			BoardPostsResponseDto result = postService.findAllPost(user, boardId, keyword, pageNum);

			//then
			Page<PostsResponseDto> searchedPagedPost = result.getPost();
			verifyPost(searchedPagedPost, true, post.getTitle(), post.getContent());
		}

		private void verifyPost(
			Page<PostsResponseDto> searchedPagedPost,
			boolean isDeleted, String title, String content
		) {
			assertThat(searchedPagedPost).isNotNull();
			assertThat(searchedPagedPost.getTotalElements()).isEqualTo(1);
			assertThat(searchedPagedPost.getTotalPages()).isEqualTo(1);

			searchedPagedPost.getContent().forEach(
				postResponseDto -> {
					assertThat(postResponseDto).isNotNull();
					assertAll(
						() -> assertThat(postResponseDto.getIsDeleted()).isEqualTo(isDeleted),
						() -> assertThat(postResponseDto.getTitle()).isEqualTo(title),
						() -> assertThat(postResponseDto.getContent()).isEqualTo(content),
						() -> assertThat(postResponseDto.getWriterNickname()).isEqualTo(user.getNickname())
					);
				});
		}
	}
}
