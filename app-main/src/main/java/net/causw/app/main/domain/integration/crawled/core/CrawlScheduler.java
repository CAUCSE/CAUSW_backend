package net.causw.app.main.domain.integration.crawled.core;

import java.util.List;
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
	private final CrawlService crawlService;
	private final CrawledToPostTransferService crawledToPostTransferService;
	private final AtomicBoolean running = new AtomicBoolean(false);

	@Value("${app.crawl.local-run-on-start:false}")
	private boolean runOnStart;

	/**
	 * 애플리케이션 시작 시 로컬 즉시 실행 설정이 활성화되어 있으면 전체 사이트 크롤링을 실행합니다.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationStart() {
		if (runOnStart) {
			runAllSites();
		}
	}

	/**
	 * 활성 사이트를 크롤링하고 수집된 공지를 Post로 변환합니다.
	 * 중복 실행이 감지되면 현재 실행을 건너뜁니다.
	 */
	@Scheduled(cron = "${app.crawl.cron:0 0 * * * *}", zone = "${app.crawl.zone:Asia/Seoul}")
	public void runAllSites() {
		if (!running.compareAndSet(false, true)) {
			log.warn("[크롤링] 중복 실행이 감지되어 이번 실행을 건너뜁니다.");
			return;
		}

		try {
			List<CrawlResult> results = crawlService.crawlAllEnabled();
			crawledToPostTransferService.transferToPosts();
			results.forEach(this::logResult);
			log.info("[크롤링] 활성 사이트 크롤링 완료. 성공 사이트 수={}", results.size());
		} catch (RuntimeException e) {
			log.error("[크롤링] 스케줄 실행 실패.", e);
		} finally {
			running.set(false);
		}
	}

	/**
	 * 사이트별 크롤링 처리 결과를 로그로 기록합니다.
	 *
	 * @param result 기록할 크롤링 결과
	 */
	private void logResult(CrawlResult result) {
		log.info(
			"[크롤링] 사이트 크롤링 완료. siteId={}, 발견={}, 생성={}, 수정={}, 변경 없음={}, 실패={}",
			result.siteId(), result.discoveredCount(), result.createdCount(), result.updatedCount(),
			result.unchangedCount(), result.failedCount());
	}
}
