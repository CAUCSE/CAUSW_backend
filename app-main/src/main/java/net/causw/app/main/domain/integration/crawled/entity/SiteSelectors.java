package net.causw.app.main.domain.integration.crawled.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SiteSelectors {
	@Column(name = "article_row_selector", nullable = false)
	private String articleRow;

	@Column(name = "article_link_selector", nullable = false)
	private String articleLink;

	@Column(name = "article_category_selector", nullable = false)
	private String articleCategory;

	@Column(name = "title_selector", nullable = false)
	private String title;

	@Column(name = "body_selector", nullable = false)
	private String body;

	@Column(name = "date_selector", nullable = false)
	private String date;

	@Column(name = "author_selector", nullable = false)
	private String author;

	@Column(name = "representative_image_selector", nullable = false)
	private String representativeImage;

	@Column(name = "date_format", nullable = false)
	private String dateFormat;

	@Column(name = "next_page_selector")
	private String nextPage;

	public static SiteSelectors of(
		String articleRow,
		String articleLink,
		String articleCategory,
		String title,
		String body,
		String date,
		String author,
		String representativeImage,
		String dateFormat,
		String nextPage) {
		return SiteSelectors.builder()
			.articleRow(articleRow)
			.articleLink(articleLink)
			.articleCategory(articleCategory)
			.title(title)
			.body(body)
			.date(date)
			.author(author)
			.representativeImage(representativeImage)
			.dateFormat(dateFormat)
			.nextPage(nextPage)
			.build();
	}
}
