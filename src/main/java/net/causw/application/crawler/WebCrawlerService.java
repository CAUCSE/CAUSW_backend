package net.causw.application.crawler;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.crawled.CrawledFileLink;
import net.causw.adapter.persistence.crawled.CrawledNotice;
import net.causw.adapter.persistence.crawled.LatestCrawl;
import net.causw.adapter.persistence.repository.crawled.CrawledNoticeRepository;
import net.causw.adapter.persistence.repository.crawled.LatestCrawlRepository;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.crawl.CrawlCategory;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MeasureTime
@Service
@RequiredArgsConstructor
public class WebCrawlerService {
    private final CrawledNoticeRepository crawledNoticeRepository;
    private final LatestCrawlRepository latestCrawlRepository;

//    @Scheduled(fixedRate = 5000) // 5초마다 실행 (테스트용)
    @Scheduled(cron = "0 0 * * * *") // 매 시각 0분 0초에 실행 (배포용)
    @Transactional
    public void crawlAndSaveCAUSWNoticeSite()  {
        // 최신 URL 가져오기
        String recentNoticeLink = latestCrawlRepository.findByCrawlCategory(CrawlCategory.CAU_SW_NOTICE)
                .map(LatestCrawl::getLatestUrl)
                .orElse(null);

        int pageNum = 1;
        boolean isNew = true;
        while (isNew) {
            String url = StaticValue.CAU_CSE_BASE_URL + pageNum;
            Document doc = fetchUrl(url);

            Elements rows = doc.select("table.table-basic tbody tr");
            if (rows.isEmpty()) {
                break; // 더 이상 페이지가 없으면 종료
            }

            List<CrawledNotice> notices = new ArrayList<>();

            for (Element row : rows) {
                Element titleElement = row.select("td.aleft a").first();
                if (titleElement == null) {
                    continue;
                }

                String absoluteLink = titleElement.absUrl("href");

                if (absoluteLink.equals(recentNoticeLink)) {   // 최신 url과 비교하여 동일한 경우 for문 탈출
                    isNew = false;
                    break;
                }
                CrawledNotice notice = parseNotice(row, absoluteLink);
                notices.add(notice);
            }

            // DB save
            if (!notices.isEmpty()) {
                // 새로운 공지사항이 하나라도 있으면 저장
                crawledNoticeRepository.saveAll(notices);

                // 첫 페이지의 첫 번째 공지사항을 저장
                if (pageNum == 1) {
                    if (recentNoticeLink == null) {
                        // 처음 크롤링하는 경우엔 url save
                        latestCrawlRepository.save(LatestCrawl.of(notices.get(0).getLink(), CrawlCategory.CAU_SW_NOTICE));
                    } else {
                        // 최신 공지 URL 업데이트
                        latestCrawlRepository.updateLatestUrlByCategory(notices.get(0).getLink(), CrawlCategory.CAU_SW_NOTICE);
                    }
                }
            } else {
                // 공지가 하나도 없으면 종료
                isNew = false;
            }
            // 다음 페이지로 이동
            pageNum++;
        }
    }

    //HTML페이지를 가져옴
    protected Document fetchUrl(String url) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER, MessageUtil.FAIL_TO_CRAWL_CAU_SW_NOTICE_SITE);
        }
    }

    // 상세 페이지로 이동하여 내용 크롤링
    protected CrawledNotice parseNotice(Element row, String noticeUrl) {
        String noticeType = row.select("td span.tag").text();
        Document detailDoc = fetchUrl(noticeUrl);

        String title = detailDoc.select("div.header > h3").text();  // 제목 추출
        String announceDate = detailDoc.select("div.header > div > span").get(1).text();    // 작성일 추출
        String author = detailDoc.select("div.header > div > span").get(3).text();  // 작성자 추출

        String imageLink = detailDoc.select("div.fr-view > p > img").attr("abs:src");  // 절대경로로 이미지 추출 => 없는 경우 빈 문자열 삽입

        // 본문 내용 추출
        Element contentElement = detailDoc.select("div.fr-view").first();
        StringBuilder textWithLineBreaks = new StringBuilder();

        // 태그 기준으로 줄바꿈
        for (Element elem : contentElement.getAllElements()) {
            switch (elem.tagName()) {
                case "p":
                case "li":
                    textWithLineBreaks.append(elem.text()).append("\n");
                    break;
                case "br":
                    textWithLineBreaks.append("\n");
                    break;
            }
        }
        String content = textWithLineBreaks.toString().trim();

        List<CrawledFileLink> fileLinks = extractDownloadLink(detailDoc);
        return CrawledNotice.of(
                noticeType,
                title,
                content,
                noticeUrl,
                author,
                announceDate,
                imageLink,
                fileLinks.isEmpty() ? null : fileLinks
        );
    }

    //첨부파일 다운로드 정보를 찾고 다운로드 가능한 파일링크 생성
    private List<CrawledFileLink> extractDownloadLink(Document detailDoc) {
        List<CrawledFileLink> crawledFileLinks = new ArrayList<>();
        Elements downloadElements = detailDoc.select("div.files span");

        // 첨부파일 다운로드 경로 추출
        Elements downloadLinks = detailDoc.select("div.files span");
        Pattern pattern = Pattern.compile("goLocation\\('/_module/bbs/download.php','(\\d+)','(\\w+)'\\).*?>(.*?)<");

        for (Element link : downloadLinks) {
            // 정규식 사용
            String onclickAttr = link.attr("onclick");
            Matcher matcher = pattern.matcher(link.outerHtml());

            while (matcher.find()) {
                String uid = matcher.group(1); // uid 추출
                String code = matcher.group(2); // code 추출
                String fileName = matcher.group(3).trim(); // 파일명 추출
                // 파일 다운로드 경로 생성
                String fileUrl = String.format("https://cse.cau.ac.kr/_module/bbs/download.php?uid=%s&code=%s", uid, code);
                // CrawledFileLink 객체 생성
                crawledFileLinks.add(CrawledFileLink.of(fileName, fileUrl));
            }
        }
        return crawledFileLinks;
    }
}
