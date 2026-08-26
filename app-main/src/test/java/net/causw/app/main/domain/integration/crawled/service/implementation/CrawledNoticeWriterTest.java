package net.causw.app.main.domain.integration.crawled.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledNoticeWriter 테스트")
class CrawledNoticeWriterTest {
	@InjectMocks
	private CrawledNoticeWriter writer;

	@Mock
	private CrawledNoticeRepository repository;

	@Test
	@DisplayName("새 외부 식별자를 저장한다")
	void save_shouldCreateNotice() {
		// given
		CleanArticle article = article("hash");
		// when
		writer.save(article);

		// then
		verify(repository).save(org.mockito.ArgumentMatchers
			.argThat(notice -> notice.getSiteId().equals("site")
				&& notice.getExternalId().equals("10")
				&& notice.getTargetBoardId().equals("target-board-id")));
	}

	@Test
	@DisplayName("기존 공지를 최신 내용으로 갱신한다")
	void update_shouldUpdateExistingNotice() {
		// given
		CrawledNotice existing = existing("hash", "old-board-id");
		// when
		writer.update(existing, article("hash"));

		// then
		assertThat(existing.getTargetBoardId()).isEqualTo("target-board-id");
		assertThat(existing.getIsUpdated()).isTrue();
		verify(repository).save(existing);
	}

	private CleanArticle article(String hash) {
		return new CleanArticle(
			"site", "target-board-id", "10", "https://example.com/10", "공지", "제목", "<p>본문</p>", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), hash);
	}

	private CrawledNotice existing(String hash, String targetBoardId) {
		return CrawledNotice.of(
			"site", "10", targetBoardId, "공지", "제목", "<p>본문</p>", "https://example.com/10", "관리자",
			LocalDate.of(2026, 8, 10), null, List.of(), hash);
	}
}
