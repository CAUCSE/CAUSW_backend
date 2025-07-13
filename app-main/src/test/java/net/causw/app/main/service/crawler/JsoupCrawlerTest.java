package net.causw.app.main.service.crawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JsoupCrawlerTest {

    @InjectMocks
    private JsoupCrawler jsoupCrawler;

    @Nested
    @DisplayName("크롤링 성공 테스트")
    class CrawlSuccessTest {

        @Test
        @DisplayName("빈 목록 페이지 처리")
        void crawl_shouldReturnEmptyList_whenNoNotices() throws IOException {
            // given
            Document mockDoc = mock(Document.class);
            Elements mockRows = mock(Elements.class);

            when(mockDoc.select("table.table-basic tbody tr")).thenReturn(mockRows);
            when(mockRows.isEmpty()).thenReturn(true);

            try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
                Connection mockConnection = mock(Connection.class);
                mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
                when(mockConnection.userAgent(anyString())).thenReturn(mockConnection);
                when(mockConnection.get()).thenReturn(mockDoc);

                // when
                List<CrawledNotice> result = jsoupCrawler.crawl();

                // then
                assertThat(result).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("크롤링 실패 테스트")
    class CrawlFailureTest {

        @Test
        @DisplayName("네트워크 오류 시 재시도 후 예외 발생")
        void crawl_shouldThrowException_whenNetworkError() throws IOException {
            // given
            try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
                Connection mockConnection = mock(Connection.class);
                mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
                when(mockConnection.userAgent(anyString())).thenReturn(mockConnection);
                when(mockConnection.get()).thenThrow(new IOException("Network error"));

                // when & then
                assertThatThrownBy(() -> jsoupCrawler.crawl())
                    .isInstanceOf(InternalServerException.class)
                    .hasMessageContaining(MessageUtil.FAIL_TO_CRAWL_CAU_SW_NOTICE_SITE)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INTERNAL_SERVER);

                // 재시도 확인 (MAX_RETRIES * 페이지 수)
                verify(mockConnection, atLeast(StaticValue.CRAWLING_MAX_RETRIES)).get();
            }
        }
    }
} 