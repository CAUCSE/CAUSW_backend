package net.causw.app.main.domain.community.post.repository.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;

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

	@DisplayName("명시적인 게시판 필터가 비어 있으면 DB 조회 없이 빈 Slice를 반환한다")
	@Test
	void findPostsWithCursor_shouldReturnEmptySlice_whenBoardFilterIsEmpty() {
		PostReadQueryContext readContext = new PostReadQueryContext(
			"user-id",
			false,
			Set.of(BoardReadScope.BOTH),
			Set.of());

		Slice<PostCursorResult> result = postQueryRepository.findPostsWithCursor(
			List.of(),
			readContext,
			null,
			null,
			20,
			null);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.hasNext()).isFalse();
		assertThat(result.getPageable().getPageSize()).isEqualTo(20);
		verifyNoInteractions(jpaQueryFactory);
	}
}
