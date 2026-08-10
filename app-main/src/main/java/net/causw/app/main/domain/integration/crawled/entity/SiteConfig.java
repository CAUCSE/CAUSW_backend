package net.causw.app.main.domain.integration.crawled.entity;

import java.time.Duration;
import java.util.Map;

import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.config.PaginationType;
import net.causw.app.main.domain.integration.crawled.config.StringMapJsonConverter;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_crawl_site_config")
public class SiteConfig extends BaseEntity {
	@Column(name = "site_id", nullable = false, unique = true, length = 100)
	private String siteId;

	@Enumerated(EnumType.STRING)
	@Column(name = "crawler_type", nullable = false, length = 50)
	private CrawlerType crawlerType;

	@Column(name = "list_url", nullable = false, length = 1000)
	private String listUrl;

	@Column(name = "base_url", nullable = false, length = 1000)
	private String baseUrl;

	@Convert(converter = StringMapJsonConverter.class)
	@Column(name = "request_headers", nullable = false, columnDefinition = "TEXT")
	private Map<String, String> requestHeaders;

	@Column(name = "request_delay_ms", nullable = false)
	private Long requestDelayMs;

	@Column(name = "timeout_ms", nullable = false)
	private Long timeoutMs;

	@Column(name = "max_retries", nullable = false)
	private Integer maxRetries;

	@Column(name = "max_articles", nullable = false)
	private Integer maxArticles;

	@Enumerated(EnumType.STRING)
	@Column(name = "pagination_type", nullable = false, length = 30)
	private PaginationType paginationType;

	@Column(name = "page_param", length = 100)
	private String pageParam;

	@Column(name = "max_pages", nullable = false)
	private Integer maxPages;

	@Embedded
	private SiteSelectors selectors;

	@Column(name = "requires_js_rendering", nullable = false)
	private Boolean requiresJsRendering;

	@Column(name = "requires_login", nullable = false)
	private Boolean requiresLogin;

	@Column(name = "is_enabled", nullable = false)
	private Boolean isEnabled;

	public static SiteConfig of(
		String siteId,
		CrawlerType crawlerType,
		String listUrl,
		String baseUrl,
		Map<String, String> requestHeaders,
		Duration requestDelay,
		Duration timeout,
		int maxRetries,
		int maxArticles,
		PaginationType paginationType,
		String pageParam,
		int maxPages,
		SiteSelectors selectors,
		boolean requiresJsRendering,
		boolean requiresLogin,
		boolean isEnabled) {
		return SiteConfig.builder()
			.siteId(siteId)
			.crawlerType(crawlerType)
			.listUrl(listUrl)
			.baseUrl(baseUrl)
			.requestHeaders(requestHeaders == null ? Map.of() : Map.copyOf(requestHeaders))
			.requestDelayMs(requestDelay.toMillis())
			.timeoutMs(timeout.toMillis())
			.maxRetries(maxRetries)
			.maxArticles(maxArticles)
			.paginationType(paginationType)
			.pageParam(pageParam)
			.maxPages(maxPages)
			.selectors(selectors)
			.requiresJsRendering(requiresJsRendering)
			.requiresLogin(requiresLogin)
			.isEnabled(isEnabled)
			.build();
	}

	public Duration getRequestDelay() {
		return Duration.ofMillis(requestDelayMs);
	}

	public Duration getTimeout() {
		return Duration.ofMillis(timeoutMs);
	}
}
