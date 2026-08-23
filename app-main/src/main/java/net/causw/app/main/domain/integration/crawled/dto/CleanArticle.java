package net.causw.app.main.domain.integration.crawled.dto;

import java.time.LocalDate;
import java.util.List;

public record CleanArticle(
	String siteId,
	String targetBoardId,
	String externalId,
	String sourceUrl,
	String category,
	String title,
	String contentHtml,
	String author,
	LocalDate announceDate,
	String representativeImageUrl,
	List<CleanAttachment> attachments,
	String contentHash) {
	/**
	 * 정제된 공지 데이터와 계산된 콘텐츠 해시로 저장용 DTO를 생성합니다.
	 *
	 * @param siteId 출처 사이트 식별자
	 * @param targetBoardId 저장 대상 게시판 식별자
	 * @param externalId 사이트 내부 공지 식별자
	 * @param sourceUrl 원문 URL
	 * @param category 공지 카테고리
	 * @param title 공지 제목
	 * @param contentHtml 정제된 본문 HTML
	 * @param author 작성자
	 * @param announceDate 공지일
	 * @param representativeImageUrl 대표 이미지 URL
	 * @param attachments 정제된 첨부파일 목록
	 * @param contentHash 변경 감지용 콘텐츠 해시
	 * @return 콘텐츠 해시가 포함된 정제 공지
	 */
	public static CleanArticle of(
		String siteId,
		String targetBoardId,
		String externalId,
		String sourceUrl,
		String category,
		String title,
		String contentHtml,
		String author,
		LocalDate announceDate,
		String representativeImageUrl,
		List<CleanAttachment> attachments,
		String contentHash) {
		List<CleanAttachment> copiedAttachments = List.copyOf(attachments);
		return new CleanArticle(
			siteId, targetBoardId, externalId, sourceUrl, category, title, contentHtml, author, announceDate,
			representativeImageUrl, copiedAttachments, contentHash);
	}
}
