package net.causw.app.main.domain.community.post.repository.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;

import com.querydsl.jpa.impl.JPAQueryFactory;

@ExtendWith(MockitoExtension.class)
class PostQueryRepositoryTest {

	@Mock
	private JPAQueryFactory jpaQueryFactory;

	private PostQueryRepository postQueryRepository;

	@BeforeEach
	void setUp() {
		postQueryRepository = new PostQueryRepository(jpaQueryFactory);
	}

	@Nested
	@DisplayName("마이페이지 커서 조회 테스트")
	class MyPageCursorQueryTest {

		@DisplayName("접근 가능한 게시판이 없으면 내가 댓글 단 글 조회는 DB 조회 없이 빈 Slice를 반환한다")
		@Test
		void findPostsCommentedByUserWithCursor_shouldReturnEmptySlice_whenNoAccessibleBoards() {
			// when
			Slice<PostCursorResult> result = postQueryRepository.findPostsCommentedByUserWithCursor(
				"user-id",
				Set.of(),
				List.of(),
				null,
				null,
				20);

			// then
			assertEmptySlice(result, 20);
			verifyNoInteractions(jpaQueryFactory);
		}

		@DisplayName("접근 가능한 게시판 목록이 null이면 내가 댓글 단 글 조회는 DB 조회 없이 빈 Slice를 반환한다")
		@Test
		void findPostsCommentedByUserWithCursor_shouldReturnEmptySlice_whenAccessibleBoardsAreNull() {
			// when
			Slice<PostCursorResult> result = postQueryRepository.findPostsCommentedByUserWithCursor(
				"user-id",
				Set.of(),
				null,
				null,
				null,
				20);

			// then
			assertEmptySlice(result, 20);
			verifyNoInteractions(jpaQueryFactory);
		}

		@DisplayName("접근 가능한 게시판이 없으면 내가 작성한 글 조회는 DB 조회 없이 빈 Slice를 반환한다")
		@Test
		void findPostsWrittenByUserWithCursor_shouldReturnEmptySlice_whenNoAccessibleBoards() {
			// when
			Slice<PostCursorResult> result = postQueryRepository.findPostsWrittenByUserWithCursor(
				"user-id",
				List.of(),
				null,
				null,
				20);

			// then
			assertEmptySlice(result, 20);
			verifyNoInteractions(jpaQueryFactory);
		}

		@DisplayName("접근 가능한 게시판 목록이 null이면 내가 작성한 글 조회는 DB 조회 없이 빈 Slice를 반환한다")
		@Test
		void findPostsWrittenByUserWithCursor_shouldReturnEmptySlice_whenAccessibleBoardsAreNull() {
			// when
			Slice<PostCursorResult> result = postQueryRepository.findPostsWrittenByUserWithCursor(
				"user-id",
				null,
				null,
				null,
				20);

			// then
			assertEmptySlice(result, 20);
			verifyNoInteractions(jpaQueryFactory);
		}

		@DisplayName("접근 가능한 게시판이 없으면 내가 좋아요한 글 조회는 DB 조회 없이 빈 Slice를 반환한다")
		@Test
		void findPostsLikedByUserWithCursor_shouldReturnEmptySlice_whenNoAccessibleBoards() {
			// when
			Slice<PostCursorResult> result = postQueryRepository.findPostsLikedByUserWithCursor(
				"user-id",
				Set.of(),
				List.of(),
				null,
				null,
				20);

			// then
			assertEmptySlice(result, 20);
			verifyNoInteractions(jpaQueryFactory);
		}

		@DisplayName("접근 가능한 게시판 목록이 null이면 내가 좋아요한 글 조회는 DB 조회 없이 빈 Slice를 반환한다")
		@Test
		void findPostsLikedByUserWithCursor_shouldReturnEmptySlice_whenAccessibleBoardsAreNull() {
			// when
			Slice<PostCursorResult> result = postQueryRepository.findPostsLikedByUserWithCursor(
				"user-id",
				Set.of(),
				null,
				null,
				null,
				20);

			// then
			assertEmptySlice(result, 20);
			verifyNoInteractions(jpaQueryFactory);
		}

		private void assertEmptySlice(Slice<PostCursorResult> result, int expectedSize) {
			assertThat(result.getContent()).isEmpty();
			assertThat(result.hasNext()).isFalse();
			assertThat(result.getPageable().getPageSize()).isEqualTo(expectedSize);
		}
	}
}
