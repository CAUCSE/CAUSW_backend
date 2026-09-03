package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.community.post.util.PostCategoryClassifier;
import net.causw.app.main.domain.integration.crawled.repository.query.PostCategoryBackfillTarget;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostCategoryBackfillService 테스트")
class PostCategoryBackfillServiceTest {

	@Mock
	private CrawledNoticeReader crawledNoticeReader;

	@Mock
	private PostWriter postWriter;

	private PostCategoryBackfillService service;

	@BeforeEach
	void setUp() {
		service = new PostCategoryBackfillService(crawledNoticeReader, new PostCategoryClassifier(), postWriter);
	}

	@Test
	@DisplayName("미분류 게시글을 성격별로 묶어 일괄 갱신한다")
	void backfill_shouldUpdatePostsGroupedByCategory() {
		// given
		givenChunks(List.of(
			target("notice-1", "post-1", "2026 하반기 채용 안내"),
			target("notice-2", "post-2", "하계 인턴십 모집 공고"),
			target("notice-3", "post-3", "수강신청 일정 안내")));
		given(postWriter.updateCategoryByIds(PostCategory.RECRUIT, List.of("post-1", "post-2"))).willReturn(2);
		given(postWriter.updateCategoryByIds(PostCategory.ACADEMIC, List.of("post-3"))).willReturn(1);

		// when
		int updated = service.backfill();

		// then
		assertThat(updated).isEqualTo(3);
		verify(postWriter).updateCategoryByIds(PostCategory.RECRUIT, List.of("post-1", "post-2"));
		verify(postWriter).updateCategoryByIds(PostCategory.ACADEMIC, List.of("post-3"));
	}

	@Test
	@DisplayName("분류에 실패한 게시글은 갱신 대상에서 제외한다")
	void backfill_shouldExcludeUnmatchedPosts() {
		// given
		givenChunks(List.of(target("notice-1", "post-1", "동문 소식지 발간")));

		// when
		int updated = service.backfill();

		// then
		assertThat(updated).isZero();
		verify(postWriter, never()).updateCategoryByIds(any(), any());
	}

	@Test
	@DisplayName("분류에 실패한 항목이 섞여 있어도 다음 청크를 계속 처리한다")
	void backfill_shouldContinueToNextChunkWhenSomeItemsAreUnmatched() {
		// given
		List<PostCategoryBackfillTarget> firstChunk = List.of(
			target("notice-1", "post-1", "채용 공고"),
			target("notice-2", "post-2", "동문 소식지 발간"));
		List<PostCategoryBackfillTarget> secondChunk = List.of(
			target("notice-3", "post-3", "수강신청 안내"));
		given(crawledNoticeReader.findCategoryBackfillTargets(any(), anyInt()))
			.willReturn(firstChunk, secondChunk, List.of());
		given(postWriter.updateCategoryByIds(PostCategory.RECRUIT, List.of("post-1"))).willReturn(1);
		given(postWriter.updateCategoryByIds(PostCategory.ACADEMIC, List.of("post-3"))).willReturn(1);

		// when
		int updated = service.backfill();

		// then
		assertThat(updated).isEqualTo(2);
		verify(postWriter).updateCategoryByIds(PostCategory.ACADEMIC, List.of("post-3"));
	}

	@Test
	@DisplayName("대상이 없으면 아무것도 갱신하지 않는다")
	void backfill_shouldDoNothingWhenNoTargets() {
		// given
		givenChunks(List.of());

		// when
		int updated = service.backfill();

		// then
		assertThat(updated).isZero();
		verify(postWriter, never()).updateCategoryByIds(any(), any());
	}

	private void givenChunks(List<PostCategoryBackfillTarget> firstChunk) {
		given(crawledNoticeReader.findCategoryBackfillTargets(any(), anyInt()))
			.willReturn(firstChunk, List.of());
	}

	private PostCategoryBackfillTarget target(String noticeId, String postId, String title) {
		return new PostCategoryBackfillTarget(noticeId, postId, title);
	}
}
