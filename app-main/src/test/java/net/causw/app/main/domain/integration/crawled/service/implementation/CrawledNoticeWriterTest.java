package net.causw.app.main.domain.integration.crawled.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
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

import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledNoticeWriter 테스트")
class CrawledNoticeWriterTest {
	@InjectMocks
	private CrawledNoticeWriter writer;

	@Mock
	private CrawledNoticeReader reader;

	@Mock
	private CrawledNoticeRepository repository;

	@Test
	@DisplayName("새 외부 식별자를 저장한다")
	void upsert_shouldCreate_whenSourceDoesNotExist() {
		// given
		CleanArticle article = article("hash");
		given(reader.findBySource("site", "10")).willReturn(Optional.empty());

		// when
		CrawlSaveStatus result = writer.upsert(article);

		// then
		assertThat(result).isEqualTo(CrawlSaveStatus.CREATED);
		verify(repository).save(org.mockito.ArgumentMatchers
			.argThat(notice -> notice.getSiteId().equals("site")
				&& notice.getExternalId().equals("10")
				&& notice.getTargetBoardId().equals("target-board-id")));
	}

	@Test
	@DisplayName("해시가 같으면 저장하지 않는다")
	void upsert_shouldSkip_whenHashIsSame() {
		// given
		CrawledNotice existing = existing("hash");
		given(reader.findBySource("site", "10")).willReturn(Optional.of(existing));

		// when
		CrawlSaveStatus result = writer.upsert(article("hash"));

		// then
		assertThat(result).isEqualTo(CrawlSaveStatus.UNCHANGED);
		verify(repository, never()).save(existing);
	}

	@Test
	@DisplayName("저장 대상 게시판이 바뀌면 같은 내용도 갱신한다")
	void upsert_shouldUpdate_whenTargetBoardChanges() {
		// given
		CrawledNotice existing = existing("hash", "old-board-id");
		given(reader.findBySource("site", "10")).willReturn(Optional.of(existing));

		// when
		CrawlSaveStatus result = writer.upsert(article("hash"));

		// then
		assertThat(result).isEqualTo(CrawlSaveStatus.UPDATED);
		assertThat(existing.getTargetBoardId()).isEqualTo("target-board-id");
		assertThat(existing.getIsUpdated()).isTrue();
	}

	private CleanArticle article(String hash) {
		return new CleanArticle(
			"site", "target-board-id", "10", "https://example.com/10", "공지", "제목", "<p>본문</p>", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), hash);
	}

	private CrawledNotice existing(String hash) {
		return existing(hash, "target-board-id");
	}

	private CrawledNotice existing(String hash, String targetBoardId) {
		return CrawledNotice.of(
			"site", "10", targetBoardId, "공지", "제목", "<p>본문</p>", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), hash);
	}
}
