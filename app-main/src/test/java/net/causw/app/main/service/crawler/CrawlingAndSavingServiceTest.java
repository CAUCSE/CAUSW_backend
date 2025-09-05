package net.causw.app.main.service.crawler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;
import net.causw.app.main.repository.crawled.CrawledNoticeRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CrawlingAndSavingService 테스트")
public class CrawlingAndSavingServiceTest {

	@InjectMocks
	private CrawlingAndSavingService crawlingAndSavingService;

	@Mock
	private CrawledNoticeRepository crawledNoticeRepository;

	@Mock
	private Crawler crawler;

	@Test
	@DisplayName("새 공지사항이 감지되어 저장됨")
	void crawlAndDetectUpdates_shouldSaveNewNotice() {
		// given
		CrawledNotice newNotice = CrawledNoticeFixture.newNotice();

		given(crawler.crawl()).willReturn(List.of(newNotice));
		given(crawledNoticeRepository.findByLink(newNotice.getLink()))
			.willReturn(Optional.empty());

		// when
		crawlingAndSavingService.crawlAndDetectUpdates();

		// then
		verify(crawledNoticeRepository).saveAll(any(List.class));
		assertThat(newNotice.getIsUpdated()).isTrue();
	}

	@Test
	@DisplayName("기존 공지사항 내용 변경이 감지됨")
	void crawlAndDetectUpdates_shouldDetectContentChange() {
		// given
		CrawledNotice crawledNotice = CrawledNoticeFixture.updatedNotice();
		CrawledNotice existingNotice = CrawledNoticeFixture.existingNotice();

		given(crawler.crawl()).willReturn(List.of(crawledNotice));
		given(crawledNoticeRepository.findByLink(crawledNotice.getLink()))
			.willReturn(Optional.of(existingNotice));

		// when
		crawlingAndSavingService.crawlAndDetectUpdates();

		// then
		verify(crawledNoticeRepository).saveAll(any(List.class));
		assertThat(existingNotice.getIsUpdated()).isTrue();
	}

	@Test
	@DisplayName("변경사항이 없으면 저장하지 않음")
	void crawlAndDetectUpdates_shouldNotSaveWhenContentSame() {
		// given
		CrawledNotice crawledNotice = CrawledNoticeFixture.updatedNotice();
		CrawledNotice existingNotice = CrawledNoticeFixture.existingNotice();
		// 같은 해시로 설정하여 변경 없음을 시뮬레이션
		existingNotice.updateContent(
			crawledNotice.getTitle(),
			crawledNotice.getContent(),
			crawledNotice.getContentHash()  // 크롤링된 것과 같은 해시
		);
		existingNotice.setIsUpdated(false);

		given(crawler.crawl()).willReturn(List.of(crawledNotice));
		given(crawledNoticeRepository.findByLink(crawledNotice.getLink()))
			.willReturn(Optional.of(existingNotice));

		// when
		crawlingAndSavingService.crawlAndDetectUpdates();

		// then
		verify(crawledNoticeRepository, never()).saveAll(any());
		assertThat(existingNotice.getIsUpdated()).isFalse();
	}
} 