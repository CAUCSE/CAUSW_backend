package net.causw.app.main.domain.integration.crawled.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

@DisplayName("JsoupCrawlHttpClient 테스트")
class JsoupCrawlHttpClientTest {
	private final JsoupCrawlHttpClient client = new JsoupCrawlHttpClient();

	@Test
	@DisplayName("요청이 성공하면 HTML을 반환한다")
	void fetch_shouldReturnHtml_whenRequestSucceeds() throws IOException {
		// given
		Connection connection = mock(Connection.class);
		Document document = Jsoup.parse("<p>ok</p>");
		try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
			jsoup.when(() -> Jsoup.connect("https://example.com")).thenReturn(connection);
			given(connection.headers(anyMap())).willReturn(connection);
			given(connection.timeout(anyInt())).willReturn(connection);
			given(connection.get()).willReturn(document);

			// when
			String result = client.fetch("https://example.com", config(1));

			// then
			assertThat(result).contains("<p>ok</p>");
		}
	}

	@Test
	@DisplayName("최대 횟수까지 재시도한 후 예외를 발생시킨다")
	void fetch_shouldThrowException_afterRetriesExhausted() throws IOException {
		// given
		Connection connection = mock(Connection.class);
		try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
			jsoup.when(() -> Jsoup.connect("https://example.com")).thenReturn(connection);
			given(connection.headers(anyMap())).willReturn(connection);
			given(connection.timeout(anyInt())).willReturn(connection);
			given(connection.get()).willThrow(new IOException("network"));

			// when & then
			assertThatThrownBy(() -> client.fetch("https://example.com", config(3)))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(error -> ((BaseRunTimeV2Exception)error).getErrorCode())
				.isEqualTo(IntegrationErrorCode.CRAWL_FETCH_FAILED);
			verify(connection, times(3)).get();
		}
	}

	private SiteConfig config(int maxRetries) {
		SiteConfig config = SiteConfigFixture.create();
		return SiteConfig.of(
			config.getSiteId(), config.getTargetBoardId(), config.getCrawlerType(), config.getListUrl(),
			config.getBaseUrl(),
			config.getRequestHeaders(), config.getRequestDelay(), config.getTimeout(), maxRetries,
			config.getMaxArticles(), config.getMaxScanRangeDays(), config.getPaginationType(), config.getPageParam(),
			config.getMaxPages(), false, false, true);
	}
}
