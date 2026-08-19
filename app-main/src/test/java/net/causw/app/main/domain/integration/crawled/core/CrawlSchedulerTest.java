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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import net.causw.app.main.domain.integration.crawled.dto.CrawlResult;
import net.causw.app.main.domain.integration.crawled.service.CrawlService;
import net.causw.app.main.domain.integration.crawled.service.CrawledToPostTransferService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlScheduler 테스트")
class CrawlSchedulerTest {
	@InjectMocks
	private CrawlScheduler scheduler;

	@Mock
	private CrawlService crawlService;

	@Mock
	private CrawledToPostTransferService crawledToPostTransferService;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RLock lock;

	@Test
	@DisplayName("활성 사이트 수집이 종료된 후 Post 변환을 실행한다")
	void runAllSites_shouldTransferAfterCrawling() throws InterruptedException {
		// given
		given(redissonClient.getLock("crawling.all-sites")).willReturn(lock);
		given(lock.tryLock(3, java.util.concurrent.TimeUnit.SECONDS)).willReturn(true);
		given(lock.isHeldByCurrentThread()).willReturn(true);
		given(crawlService.crawlAllEnabled())
			.willReturn(List.of(new CrawlResult("cau-sw-notice", 1, 1, 0, 0, List.of())));

		// when
		scheduler.runAllSites();

		// then
		verify(crawlService).crawlAllEnabled();
		verify(crawledToPostTransferService).transferToPosts();
		verify(lock).unlock();
	}
}
