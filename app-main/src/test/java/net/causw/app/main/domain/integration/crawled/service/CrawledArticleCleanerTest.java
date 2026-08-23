package net.causw.app.main.domain.integration.crawled.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.RawArticle;
import net.causw.app.main.domain.integration.crawled.dto.RawAttachment;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;

@DisplayName("CrawledArticleCleaner 테스트")
class CrawledArticleCleanerTest {
	private final CrawledArticleCleaner cleaner = new CrawledArticleCleaner();

	@Test
	@DisplayName("상대 URL과 이미지를 정규화하고 위험한 HTML을 제거한다")
	void clean_shouldNormalizeUrlsAndSanitizeHtml() {
		// given
		RawArticle raw = new RawArticle(
			"site", "10", "https://example.com/notices/10", " 공지 ", " 제목 ",
			"<script>alert(1)</script><p><img data-src='/images/a.png' onerror='x()'></p>",
			" 관리자 ", "2026-08-10", null,
			List.of(
				new RawAttachment(" 자료.pdf ", "/files/a.pdf"),
				new RawAttachment("중복.pdf", "/files/a.pdf")));

		// when
		CleanArticle result = cleaner.clean(raw, config());

		// then
		assertThat(result.title()).isEqualTo("제목");
		assertThat(result.targetBoardId()).isEqualTo(config().getTargetBoardId());
		assertThat(result.contentHtml()).contains("src=\"https://example.com/images/a.png\"");
		assertThat(result.contentHtml()).doesNotContain("script", "onerror", "data-src");
		assertThat(result.attachments()).singleElement()
			.satisfies(file -> assertThat(file.fileUrl()).isEqualTo("https://example.com/files/a.pdf"));
	}

	@Test
	@DisplayName("같은 입력은 같은 해시를 생성한다")
	void clean_shouldGenerateDeterministicHash() {
		// given
		RawArticle raw = new RawArticle(
			"site", "10", "https://example.com/10", "공지", "제목", "<p>본문</p>",
			"관리자", "2026-08-10", null, List.of());

		// when
		CleanArticle first = cleaner.clean(raw, config());
		CleanArticle second = cleaner.clean(raw, config());

		// then
		assertThat(first.contentHash()).isEqualTo(second.contentHash());
	}

	@Test
	@DisplayName("HTTP 또는 HTTPS가 아닌 URL은 정제하지 않는다")
	void clean_shouldRejectNonHttpUrl() {
		// given
		RawArticle raw = new RawArticle(
			"site", "10", "javascript:alert(1)", "공지", "제목", "<p>본문</p>",
			"관리자", "2026-08-10", null, List.of());

		// when & then
		assertThatThrownBy(() -> cleaner.clean(raw, config()))
			.isInstanceOf(RuntimeException.class);
	}

	private SiteConfig config() {
		return SiteConfigFixture.create();
	}
}
