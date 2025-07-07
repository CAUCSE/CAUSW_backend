package net.causw.application.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingScheduler {
    private final CrawlingAndSavingService crawlingAndSavingService;
    private final CrawledToPostTransferService crawledToPostTransferService;

    //새벽 3시 - 크롤링 및 저장
//    @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 0 3 * * *") // 새벽 3시
    public void crawlAndSave() {
        crawlingAndSavingService.crawlAndDetectUpdates();
    }

    //새벽 4시 - 게시글 변환
//    @Scheduled(fixedRate = 10000)
    @Scheduled(cron = "0 0 4 * * *") // 새벽 4시
    public void transferToPosts() {
        crawledToPostTransferService.transferToPosts();
    }
}
