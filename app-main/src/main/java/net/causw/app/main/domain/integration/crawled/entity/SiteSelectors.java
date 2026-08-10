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

	/**
	 * 사이트의 목록 및 본문 파싱에 사용할 CSS 셀렉터를 생성합니다.
	 *
	 * @param articleRow 목록의 공지 행 셀렉터
	 * @param articleLink 공지 링크 셀렉터
	 * @param articleCategory 공지 카테고리 셀렉터
	 * @param title 제목 셀렉터
	 * @param body 본문 셀렉터
	 * @param date 공지일 셀렉터
	 * @param author 작성자 셀렉터
	 * @param representativeImage 대표 이미지 셀렉터
	 * @param dateFormat 공지일 파싱 형식
	 * @param nextPage 다음 페이지 셀렉터
	 * @return 사이트 셀렉터 설정
	 */
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
