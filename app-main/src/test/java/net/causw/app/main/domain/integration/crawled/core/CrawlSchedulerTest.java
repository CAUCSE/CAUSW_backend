package net.causw.app.main.domain.integration.crawled.core;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.dto.CrawlResult;
import net.causw.app.main.domain.integration.crawled.service.CrawledToPostTransferService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlScheduler 테스트")
class CrawlSchedulerTest {
	@InjectMocks
	private CrawlScheduler scheduler;

	@Mock
	private CrawlPipeline crawlPipeline;

	@Mock
	private CrawledToPostTransferService crawledToPostTransferService;

	@Test
	@DisplayName("수집이 종료된 후 Post 변환을 실행한다")
	void runCauSwNoticeCrawl_shouldTransferAfterPipeline() {
		// given
		given(crawlPipeline.run("cau-sw-notice"))
			.willReturn(new CrawlResult("cau-sw-notice", 1, 1, 0, 0, List.of()));

		// when
		scheduler.runCauSwNoticeCrawl();

		// then
		verify(crawlPipeline).run("cau-sw-notice");
		verify(crawledToPostTransferService).transferToPosts();
	}
}
