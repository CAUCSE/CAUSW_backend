package net.causw.app.main.service.crawler;

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
