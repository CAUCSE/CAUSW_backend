package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDate;
import java.time.ZoneId;
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
import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeContentHashManager;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledNoticePersistenceService 테스트")
class CrawledNoticePersistenceServiceTest {
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

	@InjectMocks
	private CrawledNoticePersistenceService persistenceService;

	@Mock
	private CrawledNoticeReader crawledNoticeReader;
	@Mock
	private CrawledNoticeContentHashManager crawledNoticeContentHashManager;
	@Mock
	private CrawledNoticeWriter crawledNoticeWriter;
	@Mock
	private PostWriter postWriter;

	@Test
	@DisplayName("기존 공지를 한 번에 조회하여 갱신한다")
	void persistAll_shouldUpdateExistingNotice_whenSourceExists() {
		// given
		CleanArticle article = article();
		SiteConfig siteConfig = siteConfig();
		CrawledNotice notice = CrawledNotice.of(
			"site", "10", "target-board-id", "공지", "제목", "본문", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "old-hash");
		given(crawledNoticeReader.findBySources(siteConfig.getSiteId(), List.of("10")))
			.willReturn(Map.of("10", notice));
		given(crawledNoticeContentHashManager.isChanged(notice, article.contentHash())).willReturn(true);
		// when
		Map<String, CrawlSaveStatus> result = persistenceService.persistAll(siteConfig, List.of(article));

		// then
		assertThat(result).containsEntry("10", CrawlSaveStatus.UPDATED);
		verify(crawledNoticeWriter).update(notice, article);
	}

	@Test
	@DisplayName("저장 기간 밖의 신규 공지는 저장하지 않는다")
	void persistAll_shouldSkipNewNotice_whenOutsideCreationWindow() {
		// given
		CleanArticle article = article(LocalDate.now(KOREA_ZONE_ID).minusDays(4));
		SiteConfig siteConfig = siteConfig();
		given(crawledNoticeReader.findBySources(siteConfig.getSiteId(), List.of("10"))).willReturn(Map.of());

		// when
		Map<String, CrawlSaveStatus> result = persistenceService.persistAll(siteConfig, List.of(article));

		// then
		assertThat(result).containsEntry("10", CrawlSaveStatus.SKIPPED);
		verify(crawledNoticeWriter, never()).save(article);
	}

	@Test
	@DisplayName("저장 기간 안의 신규 공지는 저장한다")
	void persistAll_shouldSaveNewNotice_whenWithinCreationWindow() {
		// given
		CleanArticle article = article();
		SiteConfig siteConfig = siteConfig();
		given(crawledNoticeReader.findBySources(siteConfig.getSiteId(), List.of("10"))).willReturn(Map.of());

		// when
		Map<String, CrawlSaveStatus> result = persistenceService.persistAll(siteConfig, List.of(article));

		// then
		assertThat(result).containsEntry("10", CrawlSaveStatus.CREATED);
		verify(crawledNoticeWriter).save(article);
	}

	@Test
	@DisplayName("콘텐츠와 대상 게시판이 같으면 저장하지 않는다")
	void persistAll_shouldNotUpdate_whenContentAndTargetBoardAreUnchanged() {
		// given
		CleanArticle article = article();
		SiteConfig siteConfig = siteConfig();
		CrawledNotice notice = CrawledNotice.of(
			"site", "10", "target-board-id", "공지", "제목", "본문", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "hash");
		given(crawledNoticeReader.findBySources(siteConfig.getSiteId(), List.of("10")))
			.willReturn(Map.of("10", notice));
		given(crawledNoticeContentHashManager.isChanged(notice, article.contentHash())).willReturn(false);

		// when
		Map<String, CrawlSaveStatus> result = persistenceService.persistAll(siteConfig, List.of(article));

		// then
		assertThat(result).containsEntry("10", CrawlSaveStatus.UNCHANGED);
		verify(crawledNoticeWriter, never()).update(notice, article);
	}

	@Test
	@DisplayName("대상 게시판이 바뀌면 연결된 Post를 삭제한다")
	void persistAll_shouldSoftDeleteLinkedPost_whenTargetBoardChanges() {
		// given
		CleanArticle article = article();
		SiteConfig siteConfig = siteConfig();
		CrawledNotice notice = CrawledNotice.of(
			"site", "10", "old-board-id", "공지", "제목", "본문", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "old-hash");
		Post post = org.mockito.Mockito.mock(Post.class);
		notice.linkPost(post);
		given(crawledNoticeReader.findBySources(siteConfig.getSiteId(), List.of("10")))
			.willReturn(Map.of("10", notice));

		// when
		persistenceService.persistAll(siteConfig, List.of(article));

		// then
		verify(post).setIsDeleted(true);
		verify(postWriter).save(post);
		verify(crawledNoticeWriter).update(notice, article);
	}

	private CleanArticle article() {
		return article(LocalDate.now(KOREA_ZONE_ID));
	}

	private CleanArticle article(LocalDate announceDate) {
		return new CleanArticle(
			"site", "target-board-id", "10", "https://example.com/10", "공지", "제목", "본문", "관리자",
			announceDate, null, List.of(), "hash");
	}

	private SiteConfig siteConfig() {
		return SiteConfigFixture.create();
	}
}
