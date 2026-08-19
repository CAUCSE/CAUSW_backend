package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
@DisplayName("CrawledNoticeUpsertService 테스트")
class CrawledNoticeUpsertServiceTest {
	@InjectMocks
	private CrawledNoticeUpsertService crawledNoticeUpsertService;

	@Mock
	private CrawledNoticeReader crawledNoticeReader;

	@Mock
	private CrawledNoticeWriter crawledNoticeWriter;

	@Mock
	private PostWriter postWriter;

	@Test
	@DisplayName("대상 게시판이 변경되면 연결된 Post를 삭제하고 공지를 갱신한다")
	void upsert_shouldSoftDeleteLinkedPost_whenTargetBoardChanges() {
		// given
		CleanArticle article = article();
		CrawledNotice notice = CrawledNotice.of(
			"site", "10", "old-board-id", "공지", "제목", "본문", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "old-hash");
		Post post = org.mockito.Mockito.mock(Post.class);
		notice.linkPost(post);
		given(crawledNoticeReader.findBySource("site", "10")).willReturn(Optional.of(notice));
		given(crawledNoticeWriter.upsert(article)).willReturn(CrawlSaveStatus.UPDATED);

		// when
		CrawlSaveStatus result = crawledNoticeUpsertService.upsert(article);

		// then
		assertThat(result).isEqualTo(CrawlSaveStatus.UPDATED);
		verify(post).setIsDeleted(true);
		verify(postWriter).save(post);
		verify(crawledNoticeWriter).upsert(article);
	}

	private CleanArticle article() {
		return new CleanArticle(
			"site", "target-board-id", "10", "https://example.com/10", "공지", "제목", "본문", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), "hash");
	}
}
