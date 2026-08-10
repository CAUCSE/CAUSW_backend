package net.causw.app.main.domain.integration.crawled.crawler;

import java.util.List;

import net.causw.app.main.domain.integration.crawled.config.CrawlerType;
import net.causw.app.main.domain.integration.crawled.core.CrawlContext;
import net.causw.app.main.domain.integration.crawled.dto.ArticleUrl;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;

public interface SiteCrawler {
	CrawlerType getCrawlerType();

	List<ArticleUrl> fetchList(CrawlContext context);

	RawArticle fetchArticle(CrawlContext context, ArticleUrl articleUrl);
}
