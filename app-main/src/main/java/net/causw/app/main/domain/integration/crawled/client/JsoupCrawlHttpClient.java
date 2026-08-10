package net.causw.app.main.domain.integration.crawled.client;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JsoupCrawlHttpClient implements CrawlHttpClient {

	/** {@inheritDoc} */
	@Override
	public String fetch(String url, SiteConfig siteConfig) {
		for (int attempt = 1; attempt <= siteConfig.getMaxRetries(); attempt++) {
			try {
				String html = Jsoup.connect(url)
					.headers(siteConfig.getRequestHeaders())
					.timeout(Math.toIntExact(siteConfig.getTimeout().toMillis()))
					.get()
					.outerHtml();
				delay(siteConfig.getRequestDelay().toMillis());
				return html;
			} catch (IOException e) {
				log.warn("[크롤링] HTTP 요청 실패. siteId={}, url={}, 시도={}/{}",
					siteConfig.getSiteId(), url, attempt, siteConfig.getMaxRetries(), e);
				if (attempt < siteConfig.getMaxRetries()) {
					delay(siteConfig.getRequestDelay().toMillis() * attempt);
				}
			}
		}
		throw IntegrationErrorCode.CRAWL_FETCH_FAILED.toBaseException();
	}

	/**
	 * 현재 스레드를 지정 시간 동안 대기시키고 인터럽트 상태를 보존합니다.
	 *
	 * @param milliseconds 대기할 밀리초
	 */
	private void delay(long milliseconds) {
		if (milliseconds <= 0) {
			return;
		}
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw IntegrationErrorCode.CRAWL_INTERRUPTED.toBaseException();
		}
	}
}
