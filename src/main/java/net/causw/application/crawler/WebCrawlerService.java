package net.causw.application.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WebCrawlerService {
    // 소프트웨어학과 공지사항 & 뉴스 크롤링
//    @Scheduled(cron = "0 0 0 * * *") // 매시간 0분 0초에 실행
    @Scheduled(fixedRate = 5000) // 1000ms = 1초
    public void crawlCAUSWNoticeSite() throws IOException {
        String url = "https://cse.cau.ac.kr/sub05/sub0501.php"; // 환경 변수로 관리?

        // Jsoup으로 HTML 파싱
        Document doc = Jsoup.connect(url).get();

        // 테스트: 페이지의 제목 가져오기
        String title = doc.title();
        System.out.println("title: " + title);
    }
}
