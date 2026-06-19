package net.causw.app.main.domain.integration.crawled.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingScheduler {
	private final CrawlingAndSavingService crawlingAndSavingService;
	private final CrawledToPostTransferService crawledToPostTransferService;
	private final Environment environment;

	// local 프로필일 때 서비스 시작 시 크롤링 → 변환 순서로 즉시 수행
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationStart() {
		if (environment.matchesProfiles("local")) {
			log.info("[Local] Starting Crawling");
			crawlingAndSavingService.crawlAndDetectUpdates();
			crawledToPostTransferService.transferToPosts();
			log.info("[Local] Complete Crawling");
		}
	}

	//정시 - 크롤링 및 저장
	//    @Scheduled(fixedRate = 10000)
	@Scheduled(cron = "0 0 */1 * * *") // 매 1시간 (정시)
	public void crawlAndSave() {
		crawlingAndSavingService.crawlAndDetectUpdates();
	}

	//5분 후 - 게시글 변환
	//    @Scheduled(fixedRate = 10000)
	@Scheduled(cron = "0 5 */1 * * *") // 매 1시간 5분 후
	public void transferToPosts() {
		crawledToPostTransferService.transferToPosts();
	}
}
