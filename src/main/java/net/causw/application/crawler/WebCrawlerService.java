package net.causw.application.crawler;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.crawled.CrawledNotice;
import net.causw.adapter.persistence.repository.CrawledNoticeRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
public class WebCrawlerService {

    private final CrawledNoticeRepository crawledNoticeRepository;

    //    @Scheduled(cron = "0 0 * * * *") // 매 시각 0분 0초에 실행 (배표용)
    @Scheduled(fixedRate = 5000) // 5초마다 실행 (테스트용)
    @Transactional
    public void crawlAndSaveCAUSWNoticeSite() throws IOException {
        String url = "https://cse.cau.ac.kr/sub05/sub0501.php"; // 공지사항 목록 페이지
        Document doc = Jsoup.connect(url).get();

        Elements rows = doc.select("table.table-basic tbody tr");

        List<CrawledNotice> notices = new ArrayList<>();

        for (Element row : rows) {
            String noticeType = row.select("td span.tag").text();
            Element titleElement = row.select("td.aleft a").first();
            if (titleElement == null) {
                // 로그 저장 등 추가적인 처리가 필요할 듯
                continue;
            }
            String absoluteLink = titleElement.absUrl("href");

            // 상세 페이지로 이동하여 내용 크롤링
            Document detailDoc = Jsoup.connect(absoluteLink).get();
            // 제목 추출
            String title = detailDoc.select("div.header > h3").text();
            // 작성일 추출
            String announceDate = detailDoc.select("div.header > div > span").get(1).text();
            // 작성자 추출
            String author = detailDoc.select("div.header > div > span").get(3).text();
            // 본문 내용 추출
            String content = detailDoc.select("div.fr-view").outerHtml();

            // CrawledNotice 객체 생성
            CrawledNotice notice = CrawledNotice.of(
                    noticeType,
                    title,
                    content,
                    absoluteLink,
                    author,
                    announceDate
            );

            notices.add(notice);
        }

        // DB save
        crawledNoticeRepository.saveAll(notices);
    }
}

