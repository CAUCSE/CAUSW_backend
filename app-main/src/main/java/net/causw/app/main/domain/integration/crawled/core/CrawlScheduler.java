package net.causw.app.main.domain.integration.crawled.core;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.dto.CrawlResult;
import net.causw.app.main.domain.integration.crawled.service.CrawlService;
import net.causw.app.main.domain.integration.crawled.service.CrawledToPostTransferService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.crawl", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CrawlScheduler {
	private static final String CAU_SW_NOTICE_SITE_ID = "cau-sw-notice";

	private final CrawlService crawlService;
	private final CrawledToPostTransferService crawledToPostTransferService;
	private final AtomicBoolean running = new AtomicBoolean(false);

	@Value("${app.crawl.local-run-on-start:false}")
	private boolean runOnStart;

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationStart() {
		if (runOnStart) {
			runCauSwNoticeCrawl();
		}
	}

	@Scheduled(cron = "${app.crawl.sites.cau-sw-notice.cron:0 0 * * * *}", zone = "${app.crawl.zone:Asia/Seoul}")
	public void runCauSwNoticeCrawl() {
		if (!running.compareAndSet(false, true)) {
			log.warn("[Crawl] Skip overlapping execution. siteId={}", CAU_SW_NOTICE_SITE_ID);
			return;
		}

		try {
			CrawlResult result = crawlService.crawl(CAU_SW_NOTICE_SITE_ID);
			crawledToPostTransferService.transferToPosts();
			log.info(
				"[Crawl] Completed. siteId={}, discovered={}, created={}, updated={}, unchanged={}, failed={}",
				result.siteId(), result.discoveredCount(), result.createdCount(), result.updatedCount(),
				result.unchangedCount(), result.failedCount());
		} catch (RuntimeException e) {
			log.error("[Crawl] Site execution failed. siteId={}", CAU_SW_NOTICE_SITE_ID, e);
		} finally {
			running.set(false);
		}
	}
}
