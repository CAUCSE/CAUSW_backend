package net.causw.app.main.domain.integration.crawled.crawler.implementation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.client.CrawlHttpClient;
import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.crawler.SiteCrawler;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.dto.RawAttachment;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 중앙대학교 본교 공지 API 기반 게시판을 수집합니다.
 *
 * <p>게시판별 식별값은 {@link SiteConfig#getListUrl()}의 쿼리 파라미터에서 읽어 상세 URL을 구성합니다.</p>
 */
@Component
@Slf4j
public class CauNoticeCrawler implements SiteCrawler {
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private static final String TITLE_SELECTOR = ".lineList_v .txtL p";
	private static final String AUTHOR_SELECTOR = ".lineList_v .txtInfo .writer";
	private static final String BODY_SELECTOR = ".lineList_v .view_txt";
	private static final String IMAGE_SELECTOR = ".lineList_v .view_txt img";
	private static final String ATTACHMENT_SELECTOR = ".lineList_v .fileArea .file-download";

	protected final CrawlHttpClient crawlHttpClient;
	private final ObjectMapper objectMapper;

	public CauNoticeCrawler(CrawlHttpClient crawlHttpClient, ObjectMapper objectMapper) {
		this.crawlHttpClient = crawlHttpClient;
		this.objectMapper = objectMapper;
	}

	@Override
	public CrawlerType getCrawlerType() {
		return CrawlerType.CAU_NOTICE;
	}

	@Override
	public List<ArticleUrl> fetchList(CrawlContext context) {
		SiteConfig config = context.siteConfig();
		Map<String, ArticleUrl> targets = new LinkedHashMap<>();
		for (int page = 1; page <= config.getMaxPages() && targets.size() < config.getMaxArticles(); page++) {
			try {
				// 중앙대 공지 목록은 HTML이 아닌 전용 JSON API로 제공됩니다.
				JsonNode list = objectMapper.readTree(crawlHttpClient.fetch(config.getListUrl() + page, config))
					.path("data").path("list");
				for (JsonNode item : list) {
					String announcedAt = item.path("WRITE_DATE").asText();
					if (!isWithinScanRange(announcedAt, config))
						continue;
					String externalId = item.path("BBS_SEQ").asText();
					// 상세 URL에 목록 등록일이 없으므로 수집 대상에 함께 보관합니다.
					targets.putIfAbsent(externalId, new ArticleUrl(detailUrl(config, externalId), externalId,
						item.path("CATEGORY_NM").asText(), announcedAt));
					if (targets.size() >= config.getMaxArticles())
						break;
				}
			} catch (Exception e) {
				log.error("[크롤링] 본교 공지 목록 파싱 실패. crawlerType={}, siteId={}, page={}",
					CrawlerType.CAU_NOTICE, config.getSiteId(), page, e);
				throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
			}
		}
		return List.copyOf(targets.values());
	}

	@Override
	public RawArticle fetchArticle(CrawlContext context, ArticleUrl target) {
		SiteConfig config = context.siteConfig();
		try {
			Document document = Jsoup.parse(crawlHttpClient.fetch(target.url(), config), target.url());
			Element body = document.selectFirst(BODY_SELECTOR);
			String imageUrl = document.select(IMAGE_SELECTOR).attr("abs:src");
			return new RawArticle(config.getSiteId(), target.externalId(), target.url(), target.category(),
				requiredText(document, TITLE_SELECTOR), body == null ? "<p>내용 없음</p>" : body.html(),
				requiredText(document, AUTHOR_SELECTOR), target.announcedAt(), imageUrl,
				attachments(document, config.getBaseUrl()));
		} catch (RuntimeException e) {
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	private String detailUrl(SiteConfig config, String bbsSeq) {
		// 게시판 종류별 식별값은 목록 API URL에 저장해 crawler를 하나로 유지합니다.
		String listUrl = config.getListUrl();
		String contentsNo = value(listUrl, "CONTENTS_NO");
		String tabNo = value(listUrl, "P_TAB_NO");
		String categoryNo = value(listUrl, "BOARD_CATEGORY_NO");
		return config.getBaseUrl() + "/cms/FR_CON/BoardView.do?MENU_ID=100&CONTENTS_NO=" + contentsNo
			+ "&SITE_NO=2&P_TAB_NO=" + tabNo + "&TAB_NO=&BOARD_SEQ=4&BOARD_CATEGORY_NO=" + categoryNo
			+ "&BBS_SEQ=" + bbsSeq + "&pageNo=1";
	}

	private String value(String url, String key) {
		return java.util.Arrays.stream(url.substring(url.indexOf('?') + 1).split("&"))
			.filter(pair -> pair.startsWith(key + "="))
			.map(pair -> pair.substring(key.length() + 1))
			.findFirst()
			.orElseThrow(() -> {
				log.error("[크롤링] 본교 공지 상세 URL 구성 파라미터 누락. parameter={}", key);
				return IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
			});
	}

	private boolean isWithinScanRange(String value, SiteConfig config) {
		try {
			LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
			LocalDate today = LocalDate.now(KOREA_ZONE_ID);
			return !date.isBefore(today.minusDays(config.getMaxScanRangeDays())) && !date.isAfter(today);
		} catch (RuntimeException e) {
			log.error("[크롤링] 본교 공지 등록일 파싱 실패. crawlerType={}, siteId={}, announcedAt={}, format={}",
				CrawlerType.CAU_NOTICE, config.getSiteId(), value, DATE_FORMATTER, e);
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		}
	}

	private String requiredText(Document document, String selector) {
		Element element = document.selectFirst(selector);
		if (element == null || element.text().isBlank())
			throw IntegrationErrorCode.CRAWL_PARSE_FAILED.toBaseException();
		return element.text().trim();
	}

	private List<RawAttachment> attachments(Document document, String baseUrl) {
		return document.select(ATTACHMENT_SELECTOR).stream().map(element -> new RawAttachment(element.text().trim(),
			// 상세 HTML은 다운로드 URL 대신 파일 식별자 data attribute만 제공합니다.
			baseUrl + "/ajax/FR_SVC/FileDown.do?BOARD_SEQ=" + element.dataset().get("boardseq")
				+ "&SITE_NO=" + element.dataset().get("siteno") + "&BBS_SEQ=" + element.dataset().get("bbsseq")
				+ "&FILE_SEQ=" + element.dataset().get("fileseq")))
			.toList();
	}
}
