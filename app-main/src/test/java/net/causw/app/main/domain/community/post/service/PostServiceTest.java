package net.causw.app.main.domain.community.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.implementation.UserProfileImageReader;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardGroup;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.board.service.implementation.BoardAccessManager;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.query.PostCursorResult;
import net.causw.app.main.domain.community.post.repository.query.PostReadQueryContext;
import net.causw.app.main.domain.community.post.service.dto.ImageCreateMeta;
import net.causw.app.main.domain.community.post.service.dto.ImageUpdateMeta;
import net.causw.app.main.domain.community.post.service.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.dto.PostDetailQuery;
import net.causw.app.main.domain.community.post.service.dto.PostDetailResult;
import net.causw.app.main.domain.community.post.service.dto.PostListQuery;
import net.causw.app.main.domain.community.post.service.dto.PostListResult;
import net.causw.app.main.domain.community.post.service.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.post.service.dto.PostUpdateResult;
import net.causw.app.main.domain.community.post.service.implementation.PostImageManager;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.community.post.service.implementation.ViewCountManager;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.community.vote.service.implementation.VoteWriter;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.ProfileImageType;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.app.main.util.ObjectFixtures;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

	@InjectMocks
	PostService postService;

	@Mock
	PostReader postReader;

	@Mock
	PostWriter postWriter;

	@Mock
	PostImageManager postImageManager;

	@Mock
	BoardReader boardReader;

	@Mock
	BoardConfigReader boardConfigReader;

	@Mock
	BoardAccessManager boardAccessManager;

	@Mock
	LikePostReader likePostReader;

	@Mock
	VoteWriter voteWriter;

	@Mock
	BlockReader blockReader;

	@Mock
	UserReader userReader;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Mock
	UserProfileImageReader userProfileImageReader;

	@Mock
	ViewCountManager viewCountManager;

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
				10,
				null,
				null);
			boardAdminIds = List.of("admin-id");
		}

		@DisplayName("이미지 없이 게시글 생성 성공")
		@Test
		void createPost_shouldSucceed_withoutImages() {
			// given
			PostCreateCommand command = new PostCreateCommand(
				"테스트 게시글 제목",
				"테스트 게시글 내용",
				boardId,
				false,
				writer,
				null,
				null);

			Post mockPost = Post.of("테스트 게시글 제목", "테스트 게시글 내용", writer, false, board, List.of());
			ReflectionTestUtils.setField(mockPost, "id", "post-id");
			ReflectionTestUtils.setField(mockPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(mockPost, "updatedAt", LocalDateTime.now());

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postWriter.save(any(Post.class))).willReturn(mockPost);
			given(postImageManager.uploadAndBuildForCreate(any(Post.class), isNull(), isNull()))
				.willReturn(List.of());

			// when
			PostCreateResult result = postService.create(command);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo("post-id"),
				() -> assertThat(result.title()).isEqualTo("테스트 게시글 제목"),
				() -> assertThat(result.content()).isEqualTo("테스트 게시글 내용"),
				() -> assertThat(result.writerId()).isEqualTo("writer-id"),
				() -> assertThat(result.isAnonymous()).isFalse(),
				() -> assertThat(result.fileUrlList()).isEmpty(),
				() -> assertThat(result.boardName()).isEqualTo(board.getName()));

			verify(boardReader, times(1)).getById(boardId);
			verify(postWriter, times(1)).save(any(Post.class));
			verify(postImageManager, times(1)).uploadAndBuildForCreate(any(Post.class), isNull(), isNull());
		}

		@DisplayName("이미지와 함께 게시글 생성 성공")
		@Test
		void createPost_shouldSucceed_withImages() {
			// given
			MultipartFile mockFile = mock(MultipartFile.class);
			List<MultipartFile> images = List.of(mockFile);

			List<ImageCreateMeta> imageMetas = List.of(
				new ImageCreateMeta(0, 0, true));

			PostCreateCommand command = new PostCreateCommand(
				"테스트 게시글 제목",
				"테스트 게시글 내용",
				boardId,
				false,
				writer,
				images,
				imageMetas);

			UuidFile mockUuidFile = UuidFile.of(
				"uuid",
				"file-key",
				"https://example.com/image.jpg",
				"image.jpg",
				"jpg",
				FilePath.POST);

			PostAttachImage mockAttachImage = PostAttachImage.of(null, mockUuidFile, 0, true);

			Post mockPost = Post.of("테스트 게시글 제목", "테스트 게시글 내용", writer, false, board, List.of());
			ReflectionTestUtils.setField(mockPost, "id", "post-id");
			ReflectionTestUtils.setField(mockPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(mockPost, "updatedAt", LocalDateTime.now());

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postImageManager.uploadAndBuildForCreate(any(Post.class), eq(images), eq(imageMetas)))
				.willReturn(List.of(mockAttachImage));
			given(postWriter.save(any(Post.class))).willReturn(mockPost);

			// when
			PostCreateResult result = postService.create(command);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo("post-id"),
				() -> assertThat(result.title()).isEqualTo("테스트 게시글 제목"),
				() -> assertThat(result.content()).isEqualTo("테스트 게시글 내용"),
				() -> assertThat(result.fileUrlList()).hasSize(1),
				() -> assertThat(result.fileUrlList().get(0)).isEqualTo("https://example.com/image.jpg"));

			verify(postImageManager, times(1)).uploadAndBuildForCreate(any(Post.class), eq(images), eq(imageMetas));
			verify(postWriter, times(1)).save(any(Post.class));
		}

		@DisplayName("익명 게시글 생성 성공")
		@Test
		void createPost_shouldSucceed_asAnonymous() {
			// given
			PostCreateCommand command = new PostCreateCommand(
				"익명 게시글 제목",
				"익명 게시글",
				boardId,
				true,
				writer,
				null,
				null);

			Post mockPost = Post.of("익명 게시글 제목", "익명 게시글", writer, true, board, List.of());
			ReflectionTestUtils.setField(mockPost, "id", "post-id");
			ReflectionTestUtils.setField(mockPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(mockPost, "updatedAt", LocalDateTime.now());

			BoardConfig anonymousBoardConfig = BoardConfig.of(
				boardId, true, BoardReadScope.BOTH, BoardWriteScope.ALL_USER, false, BoardVisibility.VISIBLE, 10, null,
				null);

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(anonymousBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postWriter.save(any(Post.class))).willReturn(mockPost);
			given(postImageManager.uploadAndBuildForCreate(any(Post.class), isNull(), isNull()))
				.willReturn(List.of());

			// when
			PostCreateResult result = postService.create(command);

			// then
			assertThat(result.isAnonymous()).isTrue();
			verify(postWriter, times(1)).save(any(Post.class));
		}

		@DisplayName("비익명 게시판에 익명 게시글 작성 시 실패")
		@Test
		void createPost_shouldFail_whenAnonymousPostOnNonAnonymousBoard() {
			// given
			BoardConfig nonAnonymousBoardConfig = BoardConfig.of(
				boardId,
				false, // 비익명 게시판
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10,
				null,
				null);

			PostCreateCommand command = new PostCreateCommand(
				"익명 게시글 제목",
				"익명 게시글",
				boardId,
				true, // 익명으로 작성 시도
				writer,
				null,
				null);

			given(boardReader.getById(boardId)).willReturn(board);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(nonAnonymousBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when & then
			assertThatThrownBy(() -> postService.create(command))
				.hasMessageContaining("비익명 게시판에서 익명으로 작성할 수 없습니다.");

			verify(postWriter, never()).save(any(Post.class));
		}
	}

	@Nested
	@DisplayName("게시글 삭제 테스트")
	class DeletePostTest {

		User deleter;
		Post post;
		Board board;
		BoardConfig boardConfig;
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
			boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10,
				null,
				null);
		}

		@DisplayName("작성자가 게시글 삭제 성공")
		@Test
		void deletePost_shouldSucceed_byWriter() {
			// given
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);

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
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);

			// when
			postService.deletePost(admin, postId);

			// then
			assertThat(post.getIsDeleted()).isTrue();
		}

		@DisplayName("작성자가 이미 삭제된 게시글을 다시 삭제하면 멱등하게 성공")
		@Test
		void deletePost_shouldSucceed_whenAlreadyDeletedByWriter() {
			// given
			post.setIsDeleted(true);
			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(List.of());
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);

			// when
			postService.deletePost(deleter, postId);

			// then
			assertThat(post.getIsDeleted()).isTrue();
			verify(boardConfigReader).getByBoardId(boardId);
			verify(blockReader).existsByBlockerAndBlocked(deleter, deleter);
		}

		@DisplayName("권한 없는 사용자가 이미 삭제된 게시글을 삭제하면 실패")
		@Test
		void deletePost_shouldFail_whenAlreadyDeletedByUnauthorizedUser() {
			// given
			User unauthorizedUser = ObjectFixtures.getCertifiedUserWithId("unauthorized-id");
			post.setIsDeleted(true);
			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(List.of());
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);

			// when & then
			assertThatThrownBy(() -> postService.deletePost(unauthorizedUser, postId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.satisfies(ex -> assertThat(((BaseRunTimeV2Exception)ex).getErrorCode())
					.isEqualTo(PostErrorCode.POST_FORBIDDEN));
		}

		@DisplayName("차단 우회 권한이 없는 임원은 삭제된 게시글도 삭제할 수 없음")
		@Test
		void deletePost_shouldFail_whenAlreadyDeletedWriterIsBlockedByExecutive() {
			// given
			User president = ObjectFixtures.getCertifiedUserWithId("president-id");
			president.setRoles(Set.of(Role.PRESIDENT));
			post.setIsDeleted(true);
			given(postReader.findById(postId)).willReturn(post);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(List.of());
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(blockReader.existsByBlockerAndBlocked(president, deleter)).willReturn(true);

			// when & then
			assertThatThrownBy(() -> postService.deletePost(president, postId))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.satisfies(ex -> assertThat(((BaseRunTimeV2Exception)ex).getErrorCode())
					.isEqualTo(PostErrorCode.BLOCKED_USER_CONTENT));
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
			post = Post.of("원본 제목", "원본 내용", updater, false, board, List.of());
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
				"수정된 제목",
				"수정된 내용",
				false,
				updater,
				null,
				null);

			List<String> boardAdminIds = List.of("admin-id");
			BoardConfig boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10,
				null,
				null);

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postImageManager.mergeAndBuildForUpdate(eq(post), isNull(), isNull()))
				.willReturn(new PostImageManager.ImageUpdateResult(List.of(), List.of()));
			given(postWriter.update(
				eq(post),
				eq("수정된 제목"),
				eq("수정된 내용"),
				eq(false),
				anyList())).willReturn(post);

			// when
			PostUpdateResult result = postService.update(command);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo(postId));

			verify(postWriter, times(1)).update(
				eq(post),
				eq("수정된 제목"),
				eq("수정된 내용"),
				eq(false),
				anyList());
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

			// 기존 이미지 삭제, 새 이미지 추가 (type=NEW)
			List<ImageUpdateMeta> imageMetas = List.of(
				new ImageUpdateMeta(0, ImageUpdateMeta.Type.NEW, null, 0, true));

			PostUpdateCommand command = new PostUpdateCommand(
				postId,
				null,
				"수정된 내용",
				false,
				updater,
				newImageFiles,
				imageMetas);

			UuidFile newFile = UuidFile.of("new-uuid", "new-key", "new-url", "new.jpg", "jpg", FilePath.POST);
			ReflectionTestUtils.setField(newFile, "id", "new-file-id");

			PostAttachImage newAttachImage = PostAttachImage.of(post, newFile, 0, true);

			List<String> boardAdminIds = List.of("admin-id");
			BoardConfig boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10,
				null,
				null);

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postImageManager.mergeAndBuildForUpdate(eq(post), eq(newImageFiles), eq(imageMetas)))
				.willReturn(new PostImageManager.ImageUpdateResult(
					List.of(newAttachImage), List.of("old-file-id")));
			given(postWriter.update(
				eq(post),
				isNull(),
				eq("수정된 내용"),
				eq(false),
				anyList())).willReturn(post);

			// when
			PostUpdateResult result = postService.update(command);

			// then
			assertThat(result).isNotNull();
			verify(postImageManager, times(1)).mergeAndBuildForUpdate(eq(post), eq(newImageFiles), eq(imageMetas));
			verify(postImageManager, times(1)).deleteOrphanedFiles(List.of("old-file-id"));
		}

		@DisplayName("비익명 게시판에서 익명으로 수정 시 실패")
		@Test
		void updatePost_shouldFail_whenChangingToAnonymousOnNonAnonymousBoard() {
			// given
			Post anonymousPost = Post.of(null, "익명 게시글", updater, true, board, List.of());
			ReflectionTestUtils.setField(anonymousPost, "id", postId);
			ReflectionTestUtils.setField(anonymousPost, "createdAt", LocalDateTime.now());
			ReflectionTestUtils.setField(anonymousPost, "updatedAt", LocalDateTime.now());

			PostUpdateCommand command = new PostUpdateCommand(
				postId,
				null,
				"수정된 내용",
				true, // 익명으로 변경 시도
				updater,
				null,
				null);

			List<String> boardAdminIds = List.of("admin-id");
			BoardConfig nonAnonymousBoardConfig = BoardConfig.of(
				boardId,
				false, // 비익명 게시판
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10,
				null,
				null);

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(nonAnonymousBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when & then
			assertThatThrownBy(() -> postService.update(command))
				.hasMessageContaining("비익명 게시판에서 익명으로 작성할 수 없습니다.");

			verify(postWriter, never()).update(any(), any(), any(), any(), anyList());
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
				10,
				null,
				null);
			Mockito.lenient().when(blockReader.findBlockeeUserIdsByBlocker(viewer)).thenReturn(Set.of());
			Mockito.lenient().when(boardReader.getById(anyString()))
				.thenAnswer(invocation -> ObjectFixtures.getBoardV2WithId(invocation.getArgument(0)));

			Mockito.lenient().when(boardConfigReader.getBoardConfigMapByBoardIds(anyList()))
				.thenReturn(Map.of(boardId, boardConfig));
			Mockito.lenient().when(boardConfigReader.getAdminIdSetMapByBoardIds(anyList()))
				.thenReturn(Map.of());
		}

		@DisplayName("차단한 사용자의 게시글은 목록 조회 시 제외")
		@Test
		void getPosts_shouldExcludeBlockedUsersPosts() {
			// given
			Set<String> blockedUserIds = Set.of("blocked-writer-id");
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, null);
			List<String> boardAdminIds = List.of("admin-id");

			given(blockReader.findBlockeeUserIdsByBlocker(viewer)).willReturn(blockedUserIds);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(null)))
				.willReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 20), false));

			// when
			postService.getPosts(query);

			// then
			verify(blockReader, times(1)).findBlockeeUserIdsByBlocker(viewer);
			verify(postReader, times(1)).findPostsWithCursor(
				anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20), eq(null));
		}

		@DisplayName("특정 게시판의 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_forSpecificBoard() {
			// given
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, null);

			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id",
				"테스트 제목",
				"게시글 내용",
				5L,
				10L,
				0L,
				false,
				null,
				false,
				false,
				true,
				"viewer-id",
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
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
			given(postReader.findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.posts().get(0).postId()).isEqualTo("post-id"),
				() -> assertThat(result.posts().get(0).boardId()).isEqualTo(boardId),
				() -> assertThat(result.posts().get(0).boardName()).isEqualTo("테스트 게시판"),
				() -> assertThat(result.posts().get(0).isOwner()).isTrue(),
				() -> assertThat(result.posts().get(0).updatable()).isTrue(),
				() -> assertThat(result.posts().get(0).deletable()).isTrue(),
				() -> assertThat(result.nextCursor()).isNull());

			verify(postReader, times(1)).findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null),
				eq(null), eq(20), eq(null));
		}

		@DisplayName("삭제된 게시글은 작성자에게도 목록 수정·삭제 권한을 제공하지 않음")
		@Test
		void getPosts_shouldReturnFalsePermissionFlags_whenPostIsDeleted() {
			// given
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, null);
			PostCursorResult deletedPostResult = new PostCursorResult(
				"deleted-post-id",
				"테스트 제목",
				"삭제된 게시글",
				0L,
				0L,
				0L,
				false,
				null,
				true,
				false,
				true,
				"viewer-id",
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
				"profile-url",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(List.of());
			given(postReader.findPostsWithCursor(
				anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20), eq(null)))
				.willReturn(new SliceImpl<>(List.of(deletedPostResult), PageRequest.of(0, 20), false));
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.posts().get(0).isDeleted()).isTrue(),
				() -> assertThat(result.posts().get(0).isOwner()).isTrue(),
				() -> assertThat(result.posts().get(0).updatable()).isFalse(),
				() -> assertThat(result.posts().get(0).deletable()).isFalse());
		}

		@DisplayName("여러 게시판의 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_forMultipleBoards() {
			// given
			String boardId2 = "board-id-2";
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId, boardId2), null, null, 20, null);

			List<String> firstBoardAdminIds = List.of("viewer-id");
			List<String> secondBoardAdminIds = List.of("admin-id");
			BoardConfig boardConfig2 = BoardConfig.of(
				boardId2,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10,
				null,
				null);

			PostCursorResult postCursorResult1 = new PostCursorResult(
				"post-id-1",
				"테스트 제목",
				"게시판1 게시글 내용",
				5L,
				10L,
				0L,
				false,
				null,
				false,
				false,
				true,
				"writer-id-1",
				"작성자1",
				"닉네임1",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
				"profile-url-1",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			PostCursorResult postCursorResult2 = new PostCursorResult(
				"post-id-2",
				"테스트 제목",
				"게시판2 게시글 내용",
				3L,
				8L,
				0L,
				false,
				null,
				false,
				false,
				true,
				"writer-id-2",
				"작성자2",
				"닉네임2",
				2021,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
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
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(firstBoardAdminIds);
			given(boardConfigReader.getAdminIdsByBoardId(boardId2)).willReturn(secondBoardAdminIds);
			given(boardConfigReader.getBoardConfigMapByBoardIds(anyList()))
				.willReturn(Map.of(boardId, boardConfig, boardId2, boardConfig2));
			given(boardConfigReader.getAdminIdSetMapByBoardIds(anyList()))
				.willReturn(Map.of(boardId, Set.of("viewer-id"), boardId2, Set.of("admin-id")));
			given(postReader.findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(2),
				() -> assertThat(result.posts().get(0).postId()).isEqualTo("post-id-1"),
				() -> assertThat(result.posts().get(0).boardId()).isEqualTo(boardId),
				() -> assertThat(result.posts().get(0).boardName()).isEqualTo("테스트 게시판"),
				() -> assertThat(result.posts().get(0).updatable()).isFalse(),
				() -> assertThat(result.posts().get(0).deletable()).isTrue(),
				() -> assertThat(result.posts().get(1).postId()).isEqualTo("post-id-2"),
				() -> assertThat(result.posts().get(1).boardId()).isEqualTo(boardId2),
				() -> assertThat(result.posts().get(1).boardName()).isEqualTo("테스트 게시판2"),
				() -> assertThat(result.posts().get(1).updatable()).isFalse(),
				() -> assertThat(result.posts().get(1).deletable()).isFalse(),
				() -> assertThat(result.nextCursor()).isNull());

			verify(boardConfigReader, times(1)).getByBoardId(boardId);
			verify(boardConfigReader, times(1)).getByBoardId(boardId2);
			verify(postReader, times(1)).findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null),
				eq(null), eq(20), eq(null));
		}

		@DisplayName("커서 기반 페이징으로 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_withCursor() {
			// given
			String cursor = "2024-01-01T12:00:00|post-id-1";
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, cursor, 20, null);

			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id-2",
				"게시글 제목",
				"게시글 내용",
				5L,
				10L,
				0L,
				false,
				null,
				false,
				false,
				true,
				"writer-id",
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
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
				any(PostReadQueryContext.class),
				eq("2024-01-01T12:00:00"),
				eq("post-id-1"),
				eq(20),
				eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

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
				any(PostReadQueryContext.class),
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
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, keyword);

			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id",
				"검색 결과 제목",
				"검색어가 포함된 게시글 내용",
				5L,
				10L,
				0L,
				false,
				null,
				false,
				false,
				true,
				"writer-id",
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
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
			given(postReader.findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(keyword)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.posts().get(0).content()).contains("검색어"));

			verify(postReader, times(1)).findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null),
				eq(null), eq(20),
				eq(keyword));
		}

		@DisplayName("게시판 ID 없이 전체 게시글 목록 조회 성공")
		@Test
		void getPosts_shouldSucceed_withoutBoardId() {
			// given
			PostListQuery query = PostListQuery.of(viewer, null, null, null, 20, null);

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id",
				"테스트 제목",
				"게시글 내용",
				5L,
				10L,
				0L,
				false,
				null,
				false,
				false,
				true,
				"writer-id",
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
				"profile-url",
				LocalDateTime.now(),
				LocalDateTime.now(),
				"board-1",
				"게시판1");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(postCursorResult),
				PageRequest.of(0, 20),
				false);

			given(
				postReader.findPostsWithCursor(isNull(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
					eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1));

			verify(postReader, times(1))
				.findPostsWithCursor(isNull(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20), eq(null));
		}

		@DisplayName("전체 목록에서 조회 가능한 게시글이 없으면 빈 결과 반환")
		@Test
		void getPosts_shouldReturnEmpty_whenNoAccessibleBoards() {
			// given
			PostListQuery query = PostListQuery.of(viewer, null, null, null, 20, null);
			Slice<PostCursorResult> emptySlice = new SliceImpl<>(
				List.of(),
				PageRequest.of(0, 20),
				false);
			given(postReader.findPostsWithCursor(
				isNull(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20), eq(null)))
				.willReturn(emptySlice);

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).isEmpty(),
				() -> assertThat(result.nextCursor()).isNull());

			verify(postReader, times(1))
				.findPostsWithCursor(isNull(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20), eq(null));
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
				10,
				null,
				null);

			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, null);
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
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, null);
			List<String> boardAdminIds = List.of("admin-id");

			Slice<PostCursorResult> emptySlice = new SliceImpl<>(
				Collections.emptyList(),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(null)))
				.willReturn(emptySlice);

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).isEmpty(),
				() -> assertThat(result.nextCursor()).isNull());
		}

		@DisplayName("익명 게시글 목록 조회 시 작성자 정보 보호")
		@Test
		void getPosts_shouldSucceed_withAnonymousPost() {
			// given
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, null);
			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult anonymousPostResult = new PostCursorResult(
				"post-id",
				"익명 게시글 제목",
				"익명 게시글 내용",
				5L,
				10L,
				0L,
				true, // 익명 게시글
				null,
				false,
				false,
				true,
				"writer-id",
				"작성자",
				"닉네임",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
				"profile-url",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(anonymousPostResult),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.posts().get(0).isAnonymous()).isTrue(),
				() -> assertThat(result.posts().get(0).writerNickname()).isEqualTo("익명"),
				() -> assertThat(result.posts().get(0).writerProfileImage().profileImageType())
					.isEqualTo(ProfileImageType.GHOST),
				() -> assertThat(result.posts().get(0).writerProfileImage().profileImageUrl()).isNull());

			verify(postReader, times(1)).findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null),
				eq(null), eq(20), eq(null));
		}

		@DisplayName("익명 게시판에서 일반 게시글과 익명 게시글 혼합 조회")
		@Test
		void getPosts_shouldSucceed_withMixedAnonymousPosts() {
			// given
			PostListQuery query = PostListQuery.of(viewer, List.of(boardId), null, null, 20, null);
			List<String> boardAdminIds = List.of("admin-id");

			PostCursorResult normalPostResult = new PostCursorResult(
				"post-id-1",
				"일반 게시글 제목",
				"일반 게시글 내용",
				5L,
				10L,
				0L,
				false, // 일반 게시글
				null,
				false,
				false,
				true,
				"writer-id-1",
				"작성자1",
				"닉네임1",
				2020,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
				"profile-url-1",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			PostCursorResult anonymousPostResult = new PostCursorResult(
				"post-id-2",
				"익명 게시글 제목",
				"익명 게시글 내용",
				3L,
				8L,
				0L,
				true, // 익명 게시글
				null,
				false,
				false,
				true,
				"writer-id-2",
				"작성자2",
				"닉네임2",
				2021,
				UserState.ACTIVE,
				ProfileImageType.CUSTOM,
				"profile-url-2",
				LocalDateTime.now(),
				LocalDateTime.now(),
				boardId,
				"테스트 게시판");

			Slice<PostCursorResult> slice = new SliceImpl<>(
				List.of(normalPostResult, anonymousPostResult),
				PageRequest.of(0, 20),
				false);

			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(postReader.findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(null)))
				.willReturn(slice);
			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(2),
				// 일반 게시글 확인
				() -> assertThat(result.posts().get(0).isAnonymous()).isFalse(),
				() -> assertThat(result.posts().get(0).writerNickname()).isEqualTo("닉네임1"),
				() -> assertThat(result.posts().get(0).writerProfileImage().profileImageUrl())
					.isEqualTo("profile-url-1"),
				// 익명 게시글 확인
				() -> assertThat(result.posts().get(1).isAnonymous()).isTrue(),
				() -> assertThat(result.posts().get(1).writerNickname()).isEqualTo("익명"),
				() -> assertThat(result.posts().get(1).writerProfileImage().profileImageType())
					.isEqualTo(ProfileImageType.GHOST),
				() -> assertThat(result.posts().get(1).writerProfileImage().profileImageUrl()).isNull());
			verify(postReader, times(1)).findPostsWithCursor(anyList(), any(PostReadQueryContext.class), eq(null),
				eq(null), eq(20), eq(null));
		}

		@DisplayName("게시판 ID 없이 boardGroup(COMMUNITY)만 넘기면, 해당 탭의 접근 가능한 게시판들의 글을 통합 조회한다")
		@Test
		void getPosts_by_boardGroup_success() {
			// given
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			viewer.setAcademicStatus(AcademicStatus.ENROLLED);

			PostListQuery query = PostListQuery.of(viewer, null, BoardGroup.COMMUNITY, null, 20, null);

			String freeBoardId = "free-board-id";
			String successBoardId = "success-board-id";
			Board freeBoard = ObjectFixtures.getBoardV2WithId(freeBoardId);
			Board successBoard = ObjectFixtures.getBoardV2WithId(successBoardId);

			// Mocking
			given(boardAccessManager.getReadableBoards(viewer, BoardGroup.COMMUNITY))
				.willReturn(List.of(freeBoard, successBoard));

			PostCursorResult postCursorResult = new PostCursorResult(
				"post-id", "테스트 제목", "게시글 내용", 5L, 10L, 0L, false, null, false, false, true,
				"writer-id", "작성자", "닉네임", 2020, UserState.ACTIVE, ProfileImageType.CUSTOM, "profile-url",
				LocalDateTime.now(), LocalDateTime.now(), freeBoardId, "자유 게시판");
			Slice<PostCursorResult> slice = new SliceImpl<>(List.of(postCursorResult), PageRequest.of(0, 20), false);

			// Mocking
			given(postReader.findPostsWithCursor(
				eq(List.of(freeBoardId, successBoardId)),
				any(PostReadQueryContext.class), eq(null), eq(null), eq(20), eq(null)))
				.willReturn(slice);

			given(postReader.findPostImagesByPostIds(anyList())).willReturn(Map.of());
			given(likePostReader.getLikedPostIds(anyString(), anyList())).willReturn(Set.of());

			// when
			PostListResult result = postService.getPosts(query);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.posts()).hasSize(1),
				() -> assertThat(result.posts().get(0).boardId()).isEqualTo(freeBoardId));

			// Verify
			verify(boardAccessManager, times(1)).getReadableBoards(viewer, BoardGroup.COMMUNITY);
			verify(postReader, times(1)).findPostsWithCursor(
				eq(List.of(freeBoardId, successBoardId)), any(PostReadQueryContext.class), eq(null), eq(null), eq(20),
				eq(null));
		}
	}

	@Nested
	@DisplayName("마이페이지 게시글 목록 조회 테스트")
	class GetMyPostsTest {

		User viewer;
		int pageSize;

		@BeforeEach
		void setUp() {
			viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			pageSize = 20;
		}

		@DisplayName("개인 목록은 시스템 관리자와 차단 정보를 포함한 공통 읽기 컨텍스트를 전달한다")
		@Test
		void myPostLists_shouldPassCommonReadContext_forSystemAdmin() {
			// given
			viewer.setRoles(Set.of(Role.ADMIN));
			Set<String> blockedWriterIds = Set.of("blocked-writer-id");
			Slice<PostCursorResult> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, pageSize), false);
			given(blockReader.findBlockeeUserIdsByBlocker(viewer)).willReturn(blockedWriterIds);
			given(postReader.findPostsCommentedByUserWithCursor(
				eq(viewer.getId()), any(PostReadQueryContext.class), eq(null), eq(null), eq(pageSize)))
				.willReturn(emptySlice);
			given(postReader.findPostsWrittenByUserWithCursor(
				eq(viewer.getId()), any(PostReadQueryContext.class), eq(null), eq(null), eq(pageSize)))
				.willReturn(emptySlice);
			given(postReader.findPostsLikedByUserWithCursor(
				eq(viewer.getId()), any(PostReadQueryContext.class), eq(null), eq(null), eq(pageSize)))
				.willReturn(emptySlice);

			// when
			PostListResult commented = postService.getPostsCommentedByUser(viewer, null, pageSize);
			PostListResult written = postService.getPostsWrittenByUser(viewer, null, pageSize);
			PostListResult liked = postService.getPostsLikedByUser(viewer, null, pageSize);

			// then
			assertAll(
				() -> assertThat(commented.posts()).isEmpty(),
				() -> assertThat(written.posts()).isEmpty(),
				() -> assertThat(liked.posts()).isEmpty());
			verify(postReader).findPostsCommentedByUserWithCursor(
				eq(viewer.getId()),
				argThat(context -> context.systemAdmin()
					&& context.blockedWriterIds().equals(blockedWriterIds)),
				eq(null), eq(null), eq(pageSize));
			verify(postReader).findPostsWrittenByUserWithCursor(
				eq(viewer.getId()),
				argThat(context -> context.systemAdmin() && context.blockedWriterIds().isEmpty()),
				eq(null), eq(null), eq(pageSize));
			verify(postReader).findPostsLikedByUserWithCursor(
				eq(viewer.getId()),
				argThat(context -> context.systemAdmin()
					&& context.blockedWriterIds().equals(blockedWriterIds)),
				eq(null), eq(null), eq(pageSize));
			verify(boardConfigReader, never()).getAccessibleBoardIdsByAcademicStatus(any());
		}

		@DisplayName("일반 사용자의 개인 목록에는 학적 범위와 차단 정보가 전달된다")
		@Test
		void getPostsCommentedByUser_shouldPassAcademicScopesAndBlockedWriters() {
			// given
			Set<String> blockedWriterIds = Set.of("blocked-writer-id");
			Slice<PostCursorResult> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, pageSize), false);
			given(blockReader.findBlockeeUserIdsByBlocker(viewer)).willReturn(blockedWriterIds);
			given(postReader.findPostsCommentedByUserWithCursor(
				eq(viewer.getId()), any(PostReadQueryContext.class), eq(null), eq(null), eq(pageSize)))
				.willReturn(emptySlice);

			// when
			PostListResult result = postService.getPostsCommentedByUser(viewer, null, pageSize);

			// then
			assertAll(
				() -> assertThat(result.posts()).isEmpty(),
				() -> assertThat(result.nextCursor()).isNull());
			verify(postReader).findPostsCommentedByUserWithCursor(
				eq(viewer.getId()),
				argThat(context -> !context.systemAdmin()
					&& context.readableScopes().equals(Set.of(BoardReadScope.BOTH, BoardReadScope.ENROLLED))
					&& context.blockedWriterIds().equals(blockedWriterIds)),
				eq(null), eq(null), eq(pageSize));
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
			ReflectionTestUtils.setField(post, "isCrawled", false);
			ReflectionTestUtils.setField(post, "isAnonymous", false);

			boardConfig = BoardConfig.of(
				boardId,
				false,
				BoardReadScope.BOTH,
				BoardWriteScope.ALL_USER,
				false,
				BoardVisibility.VISIBLE,
				10,
				null,
				null);
			Mockito.lenient().when(blockReader.existsByBlockerAndBlocked(any(), any())).thenReturn(false);

			Mockito.lenient().when(viewCountManager.hasViewedCookie(any(), anyString())).thenReturn(false);
			Mockito.lenient().when(viewCountManager.reserve(anyString(), anyString()))
				.thenReturn(new ViewCountManager.ViewReservation("key", "token", true));
		}

		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		HttpServletResponse mockResponse = mock(HttpServletResponse.class);

		@DisplayName("차단한 사용자의 게시글은 상세 조회 불가")
		@Test
		void getPostDetail_shouldFail_whenWriterIsBlocked() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(blockReader.existsByBlockerAndBlocked(viewer, writer)).willReturn(true);

			// when & then
			assertThatThrownBy(() -> postService.getPostDetail(query, mockRequest, mockResponse))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.satisfies(ex -> assertThat(((BaseRunTimeV2Exception)ex).getErrorCode())
					.isEqualTo(PostErrorCode.BLOCKED_USER_CONTENT));

			verify(likePostReader, never()).countByPostId(anyString());
		}

		@DisplayName("작성자도 차단 관계인 본인 게시글은 상세 조회 불가")
		@Test
		void getPostDetail_shouldFail_asOwner_whenBlocked() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, writer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(blockReader.existsByBlockerAndBlocked(writer, writer)).willReturn(true);

			// when & then
			assertThatThrownBy(() -> postService.getPostDetail(query, mockRequest, mockResponse))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.satisfies(ex -> assertThat(((BaseRunTimeV2Exception)ex).getErrorCode())
					.isEqualTo(PostErrorCode.BLOCKED_USER_CONTENT));
			verify(likePostReader, never()).countByPostId(anyString());
		}

		@DisplayName("게시판 관리자는 차단한 사용자의 게시글도 상세 조회 가능")
		@Test
		void getPostDetail_shouldSucceed_asBoardAdmin_evenWhenWriterIsBlocked() {
			// given
			User admin = ObjectFixtures.getCertifiedUserWithId("admin-id");
			PostDetailQuery query = new PostDetailQuery(postId, admin);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "admin-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo(postId),
				() -> assertThat(result.updatable()).isFalse(),
				() -> assertThat(result.deletable()).isTrue());
			verify(blockReader, never()).existsByBlockerAndBlocked(any(), any());
		}

		@DisplayName("게시글 상세 조회 성공")
		@Test
		void getPostDetail_shouldSucceed() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> assertThat(result.id()).isEqualTo(postId),
				() -> assertThat(result.content()).isEqualTo("게시글 내용"),
				() -> assertThat(result.numLike()).isEqualTo(10L),
				() -> assertThat(result.isPostLike()).isFalse(),
				() -> assertThat(result.isOwner()).isFalse(),
				() -> assertThat(result.updatable()).isFalse(),
				() -> assertThat(result.deletable()).isFalse(),
				() -> assertThat(result.boardId()).isEqualTo(boardId),
				() -> assertThat(result.boardName()).isNotNull());

			verify(postReader, times(2)).findByIdAndNotDeleted(postId);
		}

		@DisplayName("작성자가 게시글 상세 조회 시 수정/삭제 가능")
		@Test
		void getPostDetail_shouldSucceed_asOwner() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, writer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "writer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result.isOwner()).isTrue(),
				() -> assertThat(result.updatable()).isTrue(),
				() -> assertThat(result.deletable()).isTrue());
		}

		@DisplayName("게시판 관리자가 게시글 상세 조회 시 수정 불가·삭제 가능")
		@Test
		void getPostDetail_shouldSucceed_asBoardAdmin() {
			// given
			User admin = ObjectFixtures.getCertifiedUserWithId("admin-id");
			PostDetailQuery query = new PostDetailQuery(postId, admin);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "admin-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result.isOwner()).isFalse(),
				() -> assertThat(result.updatable()).isFalse(), // 게시판 관리자라도 수정 불가
				() -> assertThat(result.deletable()).isTrue());
		}

		@DisplayName("사용자가 좋아요한 게시글 상세 조회")
		@Test
		void getPostDetail_shouldSucceed_withLike() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(true);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result.isPostLike()).isTrue());
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

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(anonymousPost);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result.isAnonymous()).isTrue(),
				() -> assertThat(result.displayWriterNickname()).isEqualTo("익명"),
				() -> assertThat(result.writerProfileImage().profileImageType()).isEqualTo(ProfileImageType.GHOST),
				() -> assertThat(result.writerProfileImage().profileImageUrl()).isNull());
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
				10,
				null,
				null);

			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(hiddenBoardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);

			// when & then
			assertThatThrownBy(() -> postService.getPostDetail(query, mockRequest, mockResponse))
				.isInstanceOf(BaseRunTimeV2Exception.class);
		}

		@DisplayName("쿠키가 없고 예약 성공 시 조회수가 증가하고 쿠키가 발급된다")
		@Test
		void getPostDetail_shouldIncrementViewCount_whenReserveSucceeds() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			// setUp의 기본 stub(성공)을 활용하되 명시적으로 검증
			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> verify(postWriter, times(1)).incrementViewCount(postId),
				() -> verify(viewCountManager, times(1)).markViewed(mockResponse, postId));
		}

		@DisplayName("이미 쿠키가 존재하는 경우 조회수 증가와 쿠키 발급이 생략된다")
		@Test
		void getPostDetail_shouldNotIncrementViewCount_whenCookieExists() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			// 쿠키가 이미 존재한다고 설정 (setUp의 기본 stub을 덮어씀)
			given(viewCountManager.hasViewedCookie(any(), eq(postId))).willReturn(true);

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> verify(postWriter, never()).incrementViewCount(anyString()),
				() -> verify(viewCountManager, never()).markViewed(any(), anyString()));
		}

		@DisplayName("Redis 예약 획득에 실패한 경우 조회수 증가와 쿠키 발급이 생략된다")
		@Test
		void getPostDetail_shouldNotIncrementViewCount_whenReserveFails() {
			// given
			PostDetailQuery query = new PostDetailQuery(postId, viewer);
			List<String> boardAdminIds = List.of("admin-id");

			// 예약 실패(acquired = false)로 설정 (setUp의 기본 stub을 덮어씀)
			given(viewCountManager.reserve(eq(viewer.getId()), eq(postId)))
				.willReturn(new ViewCountManager.ViewReservation("key", "token", false));

			given(postReader.findByIdAndNotDeleted(postId)).willReturn(post);
			given(boardConfigReader.getByBoardId(boardId)).willReturn(boardConfig);
			given(boardConfigReader.getAdminIdsByBoardId(boardId)).willReturn(boardAdminIds);
			given(likePostReader.countByPostId(postId)).willReturn(10L);
			given(likePostReader.existsByPostIdAndUserId(postId, "viewer-id")).willReturn(false);

			// when
			PostDetailResult result = postService.getPostDetail(query, mockRequest, mockResponse);

			// then
			assertAll(
				() -> assertThat(result).isNotNull(),
				() -> verify(postWriter, never()).incrementViewCount(anyString()),
				() -> verify(viewCountManager, never()).markViewed(any(), anyString()));
		}
	}
}
