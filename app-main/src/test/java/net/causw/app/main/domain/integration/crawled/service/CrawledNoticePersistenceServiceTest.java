package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledNoticePersistenceService 테스트")
class CrawledNoticePersistenceServiceTest {
	@InjectMocks
	private CrawledNoticePersistenceService persistenceService;

	@Mock
	private CrawledNoticeReader crawledNoticeReader;
	@Mock
	private CrawledNoticeWriter crawledNoticeWriter;
	@Mock
	private PostWriter postWriter;

	@Test
	@DisplayName("기존 공지를 한 번에 조회하여 갱신한다")
	void persistAll_shouldUpdateExistingNotice_whenSourceExists() {
		// given
		CleanArticle article = article();
		CrawledNotice notice = CrawledNotice.of(
			"site", "10", "target-board-id", "공지", "제목", "본문", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "old-hash");
		given(crawledNoticeReader.findBySources("site", List.of("10"))).willReturn(Map.of("10", notice));
		given(crawledNoticeWriter.upsert(article, notice)).willReturn(CrawlSaveStatus.UPDATED);

		// when
		Map<String, CrawlSaveStatus> result = persistenceService.persistAll("site", List.of(article));

		// then
		assertThat(result).containsEntry("10", CrawlSaveStatus.UPDATED);
		verify(crawledNoticeWriter).upsert(article, notice);
	}

	@Test
	@DisplayName("대상 게시판이 바뀌면 연결된 Post를 삭제한다")
	void persistAll_shouldSoftDeleteLinkedPost_whenTargetBoardChanges() {
		// given
		CleanArticle article = article();
		CrawledNotice notice = CrawledNotice.of(
			"site", "10", "old-board-id", "공지", "제목", "본문", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "old-hash");
		Post post = org.mockito.Mockito.mock(Post.class);
		notice.linkPost(post);
		given(crawledNoticeReader.findBySources("site", List.of("10"))).willReturn(Map.of("10", notice));
		given(crawledNoticeWriter.upsert(article, notice)).willReturn(CrawlSaveStatus.UPDATED);

		// when
		persistenceService.persistAll("site", List.of(article));

		// then
		verify(post).setIsDeleted(true);
		verify(postWriter).save(post);
	}

	private CleanArticle article() {
		return new CleanArticle(
			"site", "target-board-id", "10", "https://example.com/10", "공지", "제목", "본문", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "hash");
	}
}
