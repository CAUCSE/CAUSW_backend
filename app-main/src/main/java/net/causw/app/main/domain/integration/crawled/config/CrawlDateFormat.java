package net.causw.app.main.domain.integration.crawled.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public enum CrawlDateFormat {
	YYYY_MM_DD("yyyy-MM-dd"),
	YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss");

	private final DateTimeFormatter formatter;

	CrawlDateFormat(String pattern) {
		this.formatter = DateTimeFormatter.ofPattern(pattern);
	}

	public static CrawlDateFormat resolve(CrawlerType crawlerType) {
		return switch (crawlerType) {
			case CAU_SW_NOTICE -> YYYY_MM_DD;
			case CAU_AI_NOTICE -> YYYY_MM_DD_HH_MM_SS;
		};
	}

	public LocalDate parse(String value) {
		return formatter.parse(value.trim(), LocalDate::from);
	}
}
