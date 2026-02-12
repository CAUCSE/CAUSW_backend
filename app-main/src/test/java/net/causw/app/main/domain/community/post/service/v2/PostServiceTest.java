package net.causw.app.main.domain.community.post.service.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
import net.causw.app.main.domain.asset.file.service.v2.implementation.PostAttachImageWriter;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.query.PostCursorResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostWriter;
import net.causw.app.main.domain.community.reaction.service.implementation.FavoritePostReader;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.community.vote.service.implementation.VoteWriter;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

	@InjectMocks
	PostService postService;

	@Mock
	PostReader postReader;

	@Mock
	PostWriter postWriter;

	@Mock
	BoardReader boardReader;

	@Mock
	FileWriter fileWriter;

	@Mock
	FileReader fileReader;

	@Mock
	BoardConfigReader boardConfigReader;

	@Mock
	LikePostReader likePostReader;

	@Mock
	FavoritePostReader favoritePostReader;

	@Mock
	PostAttachImageWriter postAttachImageWriter;

	@Mock
	VoteWriter voteWriter;

	@Nested
	@DisplayName("게시글 생성 테스트")
	class CreatePostTest {

		User writer;
		Board board;
		BoardConfig boardConfig;
		List<String> boardAdminIds;
		String boardId;

		@BeforeEach
		void setUp() {
			writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
			boardId = "board-id";
			board = ObjectFixtures.getBoardV2WithId(boardId);
			boardConfig = BoardConfig.of(
				boardId,
				false, // 일반 게시판 (비익명)
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);
			boardAdminIds = List.of("admin-id");
		}

		@DisplayName("이미지 없이 게시글 생성 성공")
		@Test
		void createPost_shouldSucceed_withoutImages() {
			// given
			PostCreateCommand command = new PostCreateCommand(
				"테스트 게시글 내용",
				boardId,
				false,
				writer,
				null);

			Post mockPost = Post.of(null, "테스트 게시글 내용", writer, false, board, List.of());
			ReflectionTestUtils.setField(mockPost, "id", "post-id");
			ReflectionTestUtils.setField(mockPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(mockPost, "updatedAt", LocalDateTime.now());

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postWriter.save(any(Post.class))).willReturn(mockPost);

			// when
			PostCreateResult result = postService.create(command);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo("post-id"),
				() -> assertThat(result.content()).isEqualTo("테스트 게시글 내용"),
				() -> assertThat(result.writerId()).isEqualTo("writer-id"),
				() -> assertThat(result.isAnonymous()).isFalse(),
				() -> assertThat(result.fileUrlList()).isEmpty(),
				() -> assertThat(result.boardName()).isEqualTo(board.getName()));

			verify(boardReader, times(1)).getById(boardId);
			verify(postWriter, times(1)).save(any(Post.class));
			verify(fileWriter, never()).uploadAndSaveList(anyList(), any(FilePath.class));
		}

		@DisplayName("이미지와 함께 게시글 생성 성공")
		@Test
		void createPost_shouldSucceed_withImages() {
			// given
			MultipartFile mockFile = mock(MultipartFile.class);
			List<MultipartFile> images = List.of(mockFile);

			PostCreateCommand command = new PostCreateCommand(
				"테스트 게시글 내용",
				boardId,
				false,
				writer,
				images);

			UuidFile mockUuidFile = UuidFile.of(
				"uuid",
				"file-key",
				"https://example.com/image.jpg",
				"image.jpg",
				"jpg",
				FilePath.POST);

			Post mockPost = Post.of(null, "테스트 게시글 내용", writer, false, board, List.of(mockUuidFile));
			ReflectionTestUtils.setField(mockPost, "id", "post-id");
			ReflectionTestUtils.setField(mockPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(mockPost, "updatedAt", LocalDateTime.now());

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(fileWriter.uploadAndSaveList(images, FilePath.POST)).willReturn(List.of(mockUuidFile));
			given(postWriter.save(any(Post.class))).willReturn(mockPost);

			// when
			PostCreateResult result = postService.create(command);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo("post-id"),
				() -> assertThat(result.content()).isEqualTo("테스트 게시글 내용"),
				() -> assertThat(result.fileUrlList()).hasSize(1),
				() -> assertThat(result.fileUrlList().get(0)).isEqualTo("https://example.com/image.jpg"));

			verify(fileWriter, times(1)).uploadAndSaveList(images, FilePath.POST);
			verify(postWriter, times(1)).save(any(Post.class));
		}

		@DisplayName("익명 게시글 생성 성공")
		@Test
		void createPost_shouldSucceed_asAnonymous() {
			// given
			PostCreateCommand command = new PostCreateCommand(
				"익명 게시글",
				boardId,
				true,
				writer,
				null);

			Post mockPost = Post.of(null, "익명 게시글", writer, true, board, List.of());
			ReflectionTestUtils.setField(mockPost, "id", "post-id");
			ReflectionTestUtils.setField(mockPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(mockPost, "updatedAt", LocalDateTime.now());

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postWriter.save(any(Post.class))).willReturn(mockPost);

			// when
			PostCreateResult result = postService.create(command);

			// then
			assertThat(result.isAnonymous()).isTrue();
			verify(postWriter, times(1)).save(any(Post.class));
		}

		@DisplayName("익명 게시판에 비익명 게시글 작성 시 실패")
		@Test
		void createPost_shouldFail_whenNonAnonymousPostOnAnonymousBoard() {
			// given
			BoardConfig anonymousBoardConfig = BoardConfig.of(
				boardId,
				true, // 익명 게시판
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);

			PostCreateCommand command = new PostCreateCommand(
				"비익명 게시글",
				boardId,
				false, // 비익명으로 작성 시도
				writer,
				null);

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(anonymousBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when & then
			assertThatThrownBy(() -> postService.create(command))
				.hasMessageContaining("익명 게시판");

			verify(postWriter, never()).save(any(Post.class));
		}
	}

	@Nested
	@DisplayName("게시글 삭제 테스트")
	class DeletePostTest {

		User deleter;
		Post post;
		Board board;
		String postId;
		String boardId;

		@BeforeEach
		void setUp() {
			deleter = ObjectFixtures.getCertifiedUserWithId("user-id");
			boardId = "board-id";
			board = ObjectFixtures.getBoardV2WithId(boardId);
			postId = "post-id";
			post = Post.of(null, "게시글 내용", deleter, false, board, List.of());
			ReflectionTestUtils.setField(post, "id", postId);
		}

		@DisplayName("작성자가 게시글 삭제 성공")
		@Test
		void deletePost_shouldSucceed_byWriter() {
			// given
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when
			postService.deletePost(deleter, postId);

			// then
			assertThat(post.getIsDeleted()).isTrue();
			verify(postReader, times(1)).findById(postId);
		}

		@DisplayName("게시판 관리자가 게시글 삭제 성공")
		@Test
		void deletePost_shouldSucceed_byBoardAdmin() {
			// given
			User admin = ObjectFixtures.getCertifiedUserWithId("admin-id");
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when
			postService.deletePost(admin, postId);

			// then
			assertThat(post.getIsDeleted()).isTrue();
		}
	}

	@Nested
	@DisplayName("게시글 수정 테스트")
	class UpdatePostTest {

		User updater;
		Post post;
		Board board;
		String postId;
		String boardId;

		@BeforeEach
		void setUp() {
			updater = ObjectFixtures.getCertifiedUserWithId("user-id");
			boardId = "board-id";
			board = ObjectFixtures.getBoardV2WithId(boardId);
			postId = "post-id";
			post = Post.of(null, "원본 내용", updater, false, board, List.of());
			ReflectionTestUtils.setField(post, "id", postId);
			ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now());
		}

		@DisplayName("게시글 내용 수정 성공")
		@Test
		void updatePost_shouldSucceed() {
			// given
			PostUpdateCommand command = new PostUpdateCommand(
				postId,
				"수정된 내용",
				false,
				updater,
				null);

			List<String> boardAdminIds = List.of("admin-id");
			BoardConfig boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postWriter.updateContentAndImages(eq(post), eq("수정된 내용"), anyList())).willReturn(post);

			// when
			PostUpdateResult result = postService.update(command);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo(postId));

			verify(postWriter, times(1)).updateContentAndImages(eq(post), eq("수정된 내용"), anyList());
		}

		@DisplayName("게시글 이미지 교체 성공")
		@Test
		void updatePost_shouldSucceed_withNewImages() {
			// given
			UuidFile oldFile = UuidFile.of("old-uuid", "old-key", "old-url", "old.jpg", "jpg", FilePath.POST);
			ReflectionTestUtils.setField(oldFile, "id", "old-file-id");
			PostAttachImage oldImage = PostAttachImage.of(post, oldFile);
			List<PostAttachImage> oldImages = new ArrayList<>();
			oldImages.add(oldImage);
			ReflectionTestUtils.setField(post, "postAttachImageList", oldImages);

			MultipartFile mockFile = mock(MultipartFile.class);
			List<MultipartFile> newImageFiles = List.of(mockFile);

			PostUpdateCommand command = new PostUpdateCommand(
				postId,
				"수정된 내용",
				false,
				updater,
				newImageFiles);

			UuidFile newFile = UuidFile.of("new-uuid", "new-key", "new-url", "new.jpg", "jpg", FilePath.POST);
			ReflectionTestUtils.setField(newFile, "id", "new-file-id");

			List<String> boardAdminIds = List.of("admin-id");
			BoardConfig boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(fileReader.findByIds(anyList())).willReturn(List.of(oldFile));
			given(fileWriter.uploadAndSaveList(newImageFiles, FilePath.POST)).willReturn(List.of(newFile));
			given(postWriter.updateContentAndImages(eq(post), eq("수정된 내용"), anyList())).willReturn(post);

			// when
			PostUpdateResult result = postService.update(command);

			// then
			assertThat(result).isNotNull();
			verify(postAttachImageWriter, times(1)).deleteAllInBatch(anyList());
			verify(fileWriter, times(1)).deleteList(anyList());
			verify(fileWriter, times(1)).uploadAndSaveList(newImageFiles, FilePath.POST);
		}

		@DisplayName("익명 게시판에서 비익명으로 수정 시 실패")
		@Test
		void updatePost_shouldFail_whenChangingToNonAnonymousOnAnonymousBoard() {
			// given
			Post anonymousPost = Post.of(null, "익명 게시글", updater, true, board, List.of());
			ReflectionTestUtils.setField(anonymousPost, "id", postId);
			ReflectionTestUtils.setField(anonymousPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(anonymousPost, "updatedAt", LocalDateTime.now());

			PostUpdateCommand command = new PostUpdateCommand(
				postId,
				"수정된 내용",
				false, // 비익명으로 변경 시도
				updater,
				null);

			List<String> boardAdminIds = List.of("admin-id");
			BoardConfig anonymousBoardConfig = BoardConfig.of(
				boardId,
				true, // 익명 게시판
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);

			given(postReader.findById(postId)).willReturn(anonymousPost);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(anonymousBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when & then
			assertThatThrownBy(() -> postService.update(command))
				.hasMessageContaining("익명 게시판");

			verify(postWriter, never()).updateContentAndImages(any(), any(), anyList());
		}
	}

	@Nested
	@DisplayName("게시글 목록 조회 테스트")
	class GetPostsTest {

		User viewer;
		String boardId;
		BoardConfig boardConfig;

		@BeforeEach
		void setUp() {
			viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			viewer.setAcademicStatus(AcademicStatus.ENROLLED);
			boardId = "board-id";
			boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);
		}

		@DisplayName("특정 게시판의 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_forSpecificBoard() {
			// given
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, 20, null);

			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id",
				"게시글 내용",
				5L,
				10L,
				3L,
				false,
				null,
				false,
				true,
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				"profile-url",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(postCursorResult),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), eq(null), eq(null), eq(20), eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.posts().get(0).postId()).isEqualTo("post-id"),
				() -> assertThat(result.posts().get(0).boardId()).isEqualTo(boardId),
				() -> assertThat(result.posts().get(0).boardName()).isEqualTo("테스트 게시판"),
				() -> assertThat(result.nextCursor()).isNull());

			verify(postReader, times(1)).findPostsWithCursor(anyList(), eq(null), eq(null), eq(20), eq(null));
		}

		@DisplayName("여러 게시판의 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_forMultipleBoards() {
			// given
			String boardId2 = "board-id-2";
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId, boardId2), null, 20, null);

			List<String> boardAdminIds = List.of("admin-id");
			BoardConfig boardConfig2 = BoardConfig.of(
				boardId2,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);

			PostCursorResult postCursorResult1 = new PostCursorResult(
				"post-id-1",
				"게시판1 게시글 내용",
				5L,
				10L,
				3L,
				false,
				null,
				false,
				true,
				"작성자1",
				"닉네임1",
				2020,
				UserState.ACTIVE,
				"profile-url-1",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			PostCursorResult postCursorResult2 = new PostCursorResult(
				"post-id-2",
				"게시판2 게시글 내용",
				3L,
				8L,
				2L,
				false,
				null,
				false,
				true,
				"작성자2",
				"닉네임2",
				2021,
				UserState.ACTIVE,
				"profile-url-2",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId2,
				"테스트 게시판2");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(postCursorResult1, postCursorResult2),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getByBoardId(boardId2)).willReturn(boardConfig2);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(boardConfigReader.getAdminIdsByBoardId(boardId2)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), eq(null), eq(null), eq(20), eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(2),
				() -> assertThat(result.posts().get(0).postId()).isEqualTo("post-id-1"),
				() -> assertThat(result.posts().get(0).boardId()).isEqualTo(boardId),
				() -> assertThat(result.posts().get(0).boardName()).isEqualTo("테스트 게시판"),
				() -> assertThat(result.posts().get(1).postId()).isEqualTo("post-id-2"),
				() -> assertThat(result.posts().get(1).boardId()).isEqualTo(boardId2),
				() -> assertThat(result.posts().get(1).boardName()).isEqualTo("테스트 게시판2"),
				() -> assertThat(result.nextCursor()).isNull());

			verify(boardConfigReader, times(1)).getByBoardId(boardId);
			verify(boardConfigReader, times(1)).getByBoardId(boardId2);
			verify(postReader, times(1)).findPostsWithCursor(anyList(), eq(null), eq(null), eq(20), eq(null));
		}

		@DisplayName("커서 기반 페이징으로 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_withCursor() {
			// given
			String cursor = "2024-01-01T12:00:00|post-id-1";
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), cursor, 20, null);

			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id-2",
				"게시글 내용",
				5L,
				10L,
				3L,
				false,
				null,
				false,
				true,
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				"profile-url",
				LocalDateTime.of(2024, 1, 1, 11, 0),
				LocalDateTime.of(2024, 1, 1, 11, 0),
				boardId,
				"테스트 게시판");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(postCursorResult),
				PageRequest.of(0, 20),
				true);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(
				anyList(),
				eq("2024-01-01T12:00:00"),
				eq("post-id-1"),
				eq(20),
				eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.nextCursor()).isNotNull(),
				() -> assertThat(result.nextCursor()).contains("post-id-2"));

			verify(postReader, times(1)).findPostsWithCursor(
				anyList(),
				eq("2024-01-01T12:00:00"),
				eq("post-id-1"),
				eq(20),
				eq(null));
		}

		@DisplayName("키워드로 게시글 검색 성공")
		@Test
		void getPosts_shouldSucceed_withKeyword() {
			// given
			String keyword = "검색어";
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, 20, keyword);

			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id",
				"검색어가 포함된 게시글 내용",
				5L,
				10L,
				3L,
				false,
				null,
				false,
				true,
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				"profile-url",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(postCursorResult),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), eq(null), eq(null), eq(20), eq(keyword)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.posts().get(0).content()).contains("검색어"));

			verify(postReader, times(1)).findPostsWithCursor(anyList(), eq(null), eq(null), eq(20), eq(keyword));
		}

		@DisplayName("게시판 ID 없이 전체 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_withoutBoardId() {
			// given
			PostListQuery query = PostListQuery.of(viewer, null, null, 20, null);

			List<String> accessibleBoardIds = List.of("board-1", "board-2");

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id",
				"게시글 내용",
				5L,
				10L,
				3L,
				false,
				null,
				false,
				true,
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				"profile-url",
				LocalDateTime.now(),
				LocalDateTime.now(),
				"board-1",
				"게시판1");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(postCursorResult),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getAccessibleBoardIdsByAcademicStatus(AcademicStatus.ENROLLED))
				.willReturn(accessibleBoardIds);
			given(postReader.findPostsWithCursor(eq(accessibleBoardIds), eq(null), eq(null), eq(20), eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1));

			verify(boardConfigReader, times(1)).getAccessibleBoardIdsByAcademicStatus(AcademicStatus.ENROLLED);
		}

		@DisplayName("숨겨진 게시판은 관리자만 조회 가능")
		@Test
		void getPosts_shouldFail_whenBoardIsHidden() {
			// given
			BoardConfig hiddenBoardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.HIDDEN,
				10);

			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, 20, null);
			List<String> boardAdminIds = List.of("admin-id");

			given(boardConfigReader.getByBoardId(boardId)).willReturn(hiddenBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when & then
			assertThatThrownBy(() -> postService.getPosts(query))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.hasMessageContaining("게시판에 대한 권한이 없습니다");
		}

		@DisplayName("빈 결과 조회 성공")
		@Test
		void getPosts_shouldSucceed_withEmptyResult() {
			// given
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, 20, null);
			List<String> boardAdminIds = List.of("admin-id");

			Slice<PostCursorResult> emptySlice = new SliceImpl<>(
				Collections.emptyList(),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), eq(null), eq(null), eq(20), eq(null)))
				.willReturn(emptySlice);

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).isEmpty(),
				() -> assertThat(result.nextCursor()).isNull());
		}
	}

	@Nested
	@DisplayName("게시글 상세 조회 테스트")
	class GetPostDetailTest {

		User viewer;
		User writer;
		Post post;
		Board board;
		String postId;
		String boardId;
		BoardConfig boardConfig;

		@BeforeEach
		void setUp() {
			viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			writer = ObjectFixtures.getCertifiedUserWithId("writer-id");
			boardId = "board-id";
			board = ObjectFixtures.getBoardV2WithId(boardId);
			postId = "post-id";
			post = Post.of(null, "게시글 내용", writer, false, board, List.of());
			ReflectionTestUtils.setField(post, "id", postId);
			ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now());

			boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10);
		}

		@DisplayName("게시글 상세 조회 성공")
		@Test
		void getPostDetail_shouldSucceed() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(favoritePostReader.countByPostId(postId)).willReturn(3L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);
			given(favoritePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo(postId),
				() -> assertThat(result.content()).isEqualTo("게시글 내용"),
				() -> assertThat(result.numLike()).isEqualTo(10L),
				() -> assertThat(result.numFavorite()).isEqualTo(3L),
				() -> assertThat(result.isPostLike()).isFalse(),
				() -> assertThat(result.isPostFavorite()).isFalse(),
				() -> assertThat(result.isOwner()).isFalse(),
				() -> assertThat(result.updatable()).isFalse(),
				() -> assertThat(result.deletable()).isFalse(),
				() -> assertThat(result.boardId()).isEqualTo(boardId),
				() -> assertThat(result.boardName()).isNotNull());

			verify(postReader, times(1)).findById(postId);
		}

		@DisplayName("작성자가 게시글 상세 조회 시 수정/삭제 가능")
		@Test
		void getPostDetail_shouldSucceed_asOwner() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, writer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(favoritePostReader.countByPostId(postId)).willReturn(3L);
			given(likePostReader.existsByPostIdAndUserId(postId, "writer-id")).willReturn(false);
			given(favoritePostReader.existsByPostIdAndUserId(postId, "writer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query);

			// then
			assertAll(
				() -> assertThat(result.isOwner()).isTrue(),
				() -> assertThat(result.updatable()).isTrue(),
				() -> assertThat(result.deletable()).isTrue());
		}

		@DisplayName("게시판 관리자가 게시글 상세 조회 시 수정/삭제 가능")
		@Test
		void getPostDetail_shouldSucceed_asBoardAdmin() {
			// given
			User admin = ObjectFixtures.getCertifiedUserWithId("admin-id");
			PostDetailQuery query = new PostDetailQuery(postId, admin);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(favoritePostReader.countByPostId(postId)).willReturn(3L);
			given(likePostReader.existsByPostIdAndUserId(postId, "admin-id")).willReturn(false);
			given(favoritePostReader.existsByPostIdAndUserId(postId, "admin-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query);

			// then
			assertAll(
				() -> assertThat(result.isOwner()).isFalse(),
				() -> assertThat(result.updatable()).isTrue(),
				() -> assertThat(result.deletable()).isTrue());
		}

		@DisplayName("사용자가 좋아요/즐겨찾기한 게시글 상세 조회")
		@Test
		void getPostDetail_shouldSucceed_withLikeAndFavorite() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(favoritePostReader.countByPostId(postId)).willReturn(3L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(true);
			given(favoritePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(true);

			// when
			PostDetailResult result = postService.getPostDetail(query);

			// then
			assertAll(
				() -> assertThat(result.isPostLike()).isTrue(),
				() -> assertThat(result.isPostFavorite()).isTrue());
		}

		@DisplayName("익명 게시글 상세 조회 시 작성자 정보 보호")
		@Test
		void getPostDetail_shouldSucceed_withAnonymousPost() {
			// given
			Post anonymousPost = Post.of(null, "익명 게시글", writer, true, board, List.of());
			ReflectionTestUtils.setField(anonymousPost, "id", postId);
			ReflectionTestUtils.setField(anonymousPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(anonymousPost, "updatedAt", LocalDateTime.now());

			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(anonymousPost);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(favoritePostReader.countByPostId(postId)).willReturn(3L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);
			given(favoritePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query);

			// then
			assertAll(
				() -> assertThat(result.isAnonymous()).isTrue(),
				() -> assertThat(result.displayWriterNickname()).isEqualTo("익명"),
				() -> assertThat(result.writerProfileImage()).isNull());
		}

		@DisplayName("숨겨진 게시판의 게시글은 관리자만 조회 가능")
		@Test
		void getPostDetail_shouldFail_whenBoardIsHidden() {
			// given
			BoardConfig hiddenBoardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.HIDDEN,
				10);

			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(hiddenBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when & then
			assertThatThrownBy(() -> postService.getPostDetail(query))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("숨김 처리");
		}
	}
}
