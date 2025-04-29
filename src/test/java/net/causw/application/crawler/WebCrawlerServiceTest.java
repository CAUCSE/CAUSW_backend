package net.causw.application.crawler;

import net.causw.adapter.persistence.crawled.CrawledNotice;
import net.causw.adapter.persistence.crawled.LatestCrawl;
import net.causw.adapter.persistence.repository.crawled.CrawledNoticeRepository;
import net.causw.adapter.persistence.repository.crawled.LatestCrawlRepository;
import net.causw.domain.model.enums.crawl.CrawlCategory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class WebCrawlerServiceTest {

    @Spy
    @InjectMocks
    private WebCrawlerService webCrawlerService;

    @Mock
    private CrawledNoticeRepository crawledNoticeRepository;

    @Mock
    private LatestCrawlRepository latestCrawlRepository;

    @Mock private Document mockDocPage1, mockDocPage2;
    @Mock private Elements rowsNonEmpty, rowsEmpty;
    @Mock private Element mockRow, titleElement;
    @Mock private CrawledNotice mockNotice;

    @Test
    @DisplayName("최초 크롤링 시작: 신규 공지 저장 및 LatestCrawl 최초 저장")
    void crawl_FirstTime_SaveAndStoreLatest() throws Exception {
        // given
        given(latestCrawlRepository.findByCrawlCategory(CrawlCategory.CAU_SW_NOTICE))
                .willReturn(Optional.empty());
        doReturn(mockDocPage1, mockDocPage2)
                .when(webCrawlerService).fetchUrl(anyString());

        given(mockDocPage1.select("table.table-basic tbody tr"))
                .willReturn(rowsNonEmpty);
        given(rowsNonEmpty.isEmpty()).willReturn(false);
        given(rowsNonEmpty.iterator()).willReturn(List.of(mockRow).iterator());

        given(mockRow.select("td.aleft a"))
                .willReturn(new Elements(titleElement));
        given(titleElement.absUrl("href"))
                .willReturn("http://new-link");

        doReturn(mockNotice)
                .when(webCrawlerService).parseNotice(mockRow, "http://new-link");
        given(mockNotice.getLink()).willReturn("http://new-link");

        given(mockDocPage2.select("table.table-basic tbody tr"))
                .willReturn(rowsEmpty);
        given(rowsEmpty.isEmpty()).willReturn(true);

        // when
        webCrawlerService.crawlAndSaveCAUSWNoticeSite();

        // then
        verify(crawledNoticeRepository, times(1))
                .saveAll(List.of(mockNotice));
        verify(latestCrawlRepository, times(1)).save(
                argThat(latestCrawl ->
                        "http://new-link".equals(latestCrawl.getLatestUrl()) &&
                                latestCrawl.getCrawlCategory() == CrawlCategory.CAU_SW_NOTICE
                )
        );
    }

    @Test
    @DisplayName("새로운 공지가 없는 경우: 저장 및 업데이트 안 함")
    void crawl_NoNewNotices_NoSaveOrUpdate() throws Exception {
        // given
        given(latestCrawlRepository.findByCrawlCategory(CrawlCategory.CAU_SW_NOTICE))
                .willReturn(Optional.of(
                        LatestCrawl.of("http://old-link", CrawlCategory.CAU_SW_NOTICE)
                ));
        doReturn(mockDocPage1).when(webCrawlerService).fetchUrl(anyString());

        given(mockDocPage1.select("table.table-basic tbody tr"))
                .willReturn(rowsNonEmpty);
        given(rowsNonEmpty.isEmpty()).willReturn(false);
        given(rowsNonEmpty.iterator()).willReturn(List.of(mockRow).iterator());
        given(mockRow.select("td.aleft a"))
                .willReturn(new Elements(titleElement));
        given(titleElement.absUrl("href"))
                .willReturn("http://old-link");

        // when
        webCrawlerService.crawlAndSaveCAUSWNoticeSite();

        // then
        verify(crawledNoticeRepository, never()).saveAll(any());
        verify(latestCrawlRepository, never()).save(any());
        verify(latestCrawlRepository, never()).updateLatestUrlByCategory(anyString(), any());
    }

    @Test
    @DisplayName("새 공지 발견 시: LatestCrawl 업데이트")
    void crawl_ExistingLatest_NewNotice_UpdatesLatestUrl() throws Exception {
        // given: LatestCrawl에 old-link 저장
        given(latestCrawlRepository.findByCrawlCategory(CrawlCategory.CAU_SW_NOTICE))
                .willReturn(Optional.of(
                        LatestCrawl.of("http://old-link", CrawlCategory.CAU_SW_NOTICE)
                ));
        doReturn(mockDocPage1, mockDocPage2)
                .when(webCrawlerService).fetchUrl(anyString());

        given(mockDocPage1.select("table.table-basic tbody tr"))
                .willReturn(rowsNonEmpty);
        given(rowsNonEmpty.isEmpty()).willReturn(false);
        given(rowsNonEmpty.iterator()).willReturn(List.of(mockRow).iterator());
        given(mockRow.select("td.aleft a"))
                .willReturn(new Elements(titleElement));
        given(titleElement.absUrl("href"))
                .willReturn("http://new-link");

        doReturn(mockNotice)
                .when(webCrawlerService).parseNotice(mockRow, "http://new-link");
        given(mockNotice.getLink()).willReturn("http://new-link");

        given(mockDocPage2.select("table.table-basic tbody tr"))
                .willReturn(rowsEmpty);
        given(rowsEmpty.isEmpty()).willReturn(true);

        // when
        webCrawlerService.crawlAndSaveCAUSWNoticeSite();

        // then
        verify(crawledNoticeRepository, times(1)).saveAll(List.of(mockNotice));
        verify(latestCrawlRepository, times(1))
                .updateLatestUrlByCategory("http://new-link", CrawlCategory.CAU_SW_NOTICE);
        verify(latestCrawlRepository, never()).save(any());
    }
}