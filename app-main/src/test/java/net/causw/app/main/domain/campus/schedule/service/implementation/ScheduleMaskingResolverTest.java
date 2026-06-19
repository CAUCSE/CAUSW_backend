package net.causw.app.main.domain.campus.schedule.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;
import net.causw.app.main.domain.campus.schedule.service.dto.ScheduleDto;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
public class ScheduleMaskingResolverTest {

	@InjectMocks
	private ScheduleMaskingResolver scheduleMaskingResolver;

	@Mock
	private PostReader postReader;

	@Mock
	private BoardConfigReader boardConfigReader;

	private User mockUser;

	@BeforeEach
	void setUp() {
		mockUser = ObjectFixtures.getCertifiedUser();
	}

	@Nested
	@DisplayName("resolveReadablePostIds 테스트")
	class ResolveReadablePostIdsTest {

		@Test
		@DisplayName("viewer가 null이면 빈 집합을 반환하고 DB를 조회하지 않는다")
		void viewerIsNull_returnsEmptySetWithoutDbCall() {
			// given
			List<ScheduleDto> scheduleDtos = List.of(buildScheduleDto("post-1"), buildScheduleDto("post-2"));

			// when
			Set<String> result = scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, null);

			// then
			assertThat(result).isEmpty();
			verifyNoInteractions(postReader, boardConfigReader);
		}

		@Test
		@DisplayName("targetPostId가 없으면 빈 집합을 반환하고 DB를 조회하지 않는다")
		void noTargetPostIds_returnsEmptySetWithoutDbCall() {
			// given
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			List<ScheduleDto> scheduleDtos = List.of(buildScheduleDto(null), buildScheduleDto(null));

			// when
			Set<String> result = scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, viewer);

			// then
			assertThat(result).isEmpty();
			verifyNoInteractions(postReader, boardConfigReader);
		}

		@Test
		@DisplayName("조회된 게시글이 없으면 빈 집합을 반환한다")
		void noPostsFound_returnsEmptySet() {
			// given
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			List<ScheduleDto> scheduleDtos = List.of(buildScheduleDto("post-ghost"));

			given(postReader.findPostMapByIds(anyCollection())).willReturn(Map.of());

			// when
			Set<String> result = scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, viewer);

			// then
			assertThat(result).isEmpty();
			verifyNoInteractions(boardConfigReader);
		}

		@Test
		@DisplayName("게시글이 모두 삭제 상태이면 빈 집합을 반환한다")
		void allPostsDeleted_returnsEmptySet() {
			// given
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			List<ScheduleDto> scheduleDtos = List.of(buildScheduleDto("post-deleted"));

			Post deletedPost = mock(Post.class);
			given(deletedPost.getIsDeleted()).willReturn(true);
			given(postReader.findPostMapByIds(anyCollection())).willReturn(Map.of("post-deleted", deletedPost));

			// when
			Set<String> result = scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, viewer);

			// then
			assertThat(result).isEmpty();
			verifyNoInteractions(boardConfigReader);
		}

		@Test
		@DisplayName("게시판 설정이 없는 게시글은 읽기 불가 처리된다")
		void missingBoardConfig_postNotReadable() {
			// given
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			List<ScheduleDto> scheduleDtos = List.of(buildScheduleDto("post-1"));

			Post post = createPost("post-1", "board-1");
			given(postReader.findPostMapByIds(anyCollection())).willReturn(Map.of("post-1", post));
			given(boardConfigReader.getBoardConfigMapByBoardIds(anyList())).willReturn(Map.of());
			given(boardConfigReader.getAdminIdSetMapByBoardIds(anyCollection())).willReturn(Map.of());

			// when
			Set<String> result = scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, viewer);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("읽기 가능한 게시글만 포함된 집합을 반환한다")
		void mixedPermissions_returnsOnlyReadablePosts() {
			// given
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			List<ScheduleDto> scheduleDtos = List.of(
				buildScheduleDto("post-1"), // 읽기 가능 (BoardReadScope.BOTH)
				buildScheduleDto("post-2"), // 읽기 불가 (BoardReadScope.GRADUATED - ENROLLED 사용자)
				buildScheduleDto("post-3")); // DB에 없음

			Post readablePost = createPost("post-1", "board-1");
			Post forbiddenPost = createPost("post-2", "board-2");
			given(postReader.findPostMapByIds(anyCollection()))
				.willReturn(Map.of("post-1", readablePost, "post-2", forbiddenPost));

			BoardConfig readableConfig = createBoardConfig("board-1", BoardReadScope.BOTH);
			BoardConfig forbiddenConfig = createBoardConfig("board-2", BoardReadScope.GRADUATED);
			given(boardConfigReader.getBoardConfigMapByBoardIds(anyList()))
				.willReturn(Map.of("board-1", readableConfig, "board-2", forbiddenConfig));
			given(boardConfigReader.getAdminIdSetMapByBoardIds(anyCollection()))
				.willReturn(Map.of("board-1", Set.of(), "board-2", Set.of()));

			// when
			Set<String> result = scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, viewer);

			// then
			assertThat(result).containsExactly("post-1");
		}

		@Test
		@DisplayName("예상치 못한 런타임 예외는 숨기지 않고 전파한다")
		void unexpectedRuntimeException_propagates() {
			// given
			User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
			List<ScheduleDto> scheduleDtos = List.of(buildScheduleDto("post-error"));

			Post brokenPost = mock(Post.class);
			given(brokenPost.getIsDeleted()).willReturn(false);
			given(brokenPost.getBoard()).willThrow(new IllegalStateException("unexpected error"));
			given(postReader.findPostMapByIds(anyCollection())).willReturn(Map.of("post-error", brokenPost));

			// when & then
			assertThatThrownBy(() -> scheduleMaskingResolver.resolveReadablePostIds(scheduleDtos, viewer))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("unexpected error");
		}
	}

	@Nested
	@DisplayName("maskIfUnreadable 테스트")
	class MaskIfUnreadableTest {

		@Test
		@DisplayName("targetPostId가 null이면 원본 DTO를 그대로 반환한다")
		void targetPostIdIsNull_returnsOriginal() {
			// given
			ScheduleDto dto = buildScheduleDto(null);

			// when
			ScheduleDto result = scheduleMaskingResolver.maskIfUnreadable(dto, Set.of("post-1"));

			// then
			assertThat(result).isSameAs(dto);
		}

		@Test
		@DisplayName("targetPostId가 readablePostIds에 포함되면 원본 DTO를 그대로 반환한다")
		void targetPostIdIsReadable_returnsOriginal() {
			// given
			ScheduleDto dto = buildScheduleDto("post-1");

			// when
			ScheduleDto result = scheduleMaskingResolver.maskIfUnreadable(dto, Set.of("post-1"));

			// then
			assertThat(result).isSameAs(dto);
		}

		@Test
		@DisplayName("targetPostId가 readablePostIds에 없으면 targetPostId를 null로 마스킹한다")
		void targetPostIdNotReadable_masksToNull() {
			// given
			ScheduleDto dto = buildScheduleDto("post-forbidden");

			// when
			ScheduleDto result = scheduleMaskingResolver.maskIfUnreadable(dto, Set.of("post-other"));

			// then
			assertThat(result.targetPostId()).isNull();
			assertThat(result.id()).isEqualTo(dto.id());
			assertThat(result.title()).isEqualTo(dto.title());
			assertThat(result.type()).isEqualTo(dto.type());
			assertThat(result.start()).isEqualTo(dto.start());
			assertThat(result.end()).isEqualTo(dto.end());
			assertThat(result.creator()).isEqualTo(dto.creator());
		}

		@Test
		@DisplayName("readablePostIds가 비어있으면 모든 targetPostId를 null로 마스킹한다")
		void emptyReadablePostIds_masksAll() {
			// given
			ScheduleDto dto = buildScheduleDto("post-1");

			// when
			ScheduleDto result = scheduleMaskingResolver.maskIfUnreadable(dto, Set.of());

			// then
			assertThat(result.targetPostId()).isNull();
		}
	}

	// ── Fixture 헬퍼 ──────────────────────────────────────────────────────────

	private ScheduleDto buildScheduleDto(String targetPostId) {
		return ScheduleDto.builder()
			.id("schedule-id")
			.title("테스트 일정")
			.type(ScheduleType.ACADEMIC)
			.start(LocalDateTime.of(2026, 4, 15, 0, 0))
			.end(LocalDateTime.of(2026, 4, 21, 23, 59))
			.creator(mockUser)
			.targetPostId(targetPostId)
			.build();
	}

	private Post createPost(String postId, String boardId) {
		Board board = ObjectFixtures.getBoardWithId(boardId);
		Post post = ObjectFixtures.getPost(mockUser, board);
		ReflectionTestUtils.setField(post, "id", postId);
		return post;
	}

	private BoardConfig createBoardConfig(String boardId, BoardReadScope readScope) {
		return BoardConfig.of(
			boardId,
			false,
			readScope,
			BoardWriteScope.ALL_USER,
			false,
			BoardVisibility.VISIBLE,
			10,
			null,
			null);
	}
}
