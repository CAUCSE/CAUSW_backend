package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.integration.crawled.dto.CleanAttachment;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.global.util.HashUtil;

/**
 * 크롤링 공지의 변경 감지용 콘텐츠 해시 생성과 비교를 담당합니다.
 */
@Component
public class CrawledNoticeContentHashManager {
	private static final char FIELD_SEPARATOR = '\u001f';
	private static final char ATTACHMENT_SEPARATOR = '\u001e';

	/**
	 * 변경 감지 대상 필드와 첨부파일로 SHA-256 콘텐츠 해시를 생성합니다.
	 *
	 * @param title 공지 제목
	 * @param contentHtml 정제된 본문 HTML
	 * @param author 작성자
	 * @param announceDate 공지일
	 * @param representativeImageUrl 대표 이미지 URL
	 * @param attachments 정제된 첨부파일 목록
	 * @return 콘텐츠 해시
	 */
	public String generate(
		String title,
		String contentHtml,
		String author,
		LocalDate announceDate,
		String representativeImageUrl,
		List<CleanAttachment> attachments) {
		StringBuilder value = new StringBuilder()
			.append(title).append(FIELD_SEPARATOR)
			.append(contentHtml).append(FIELD_SEPARATOR)
			.append(author).append(FIELD_SEPARATOR)
			.append(announceDate).append(FIELD_SEPARATOR)
			.append(representativeImageUrl == null ? "" : representativeImageUrl);
		attachments.forEach(attachment -> value.append(ATTACHMENT_SEPARATOR)
			.append(attachment.fileName()).append(FIELD_SEPARATOR).append(attachment.fileUrl()));
		return HashUtil.generateSHA256(value.toString());
	}

	/**
	 * 저장된 공지의 콘텐츠가 최신 해시 기준으로 변경되었는지 확인합니다.
	 *
	 * @param notice 저장된 공지
	 * @param latestContentHash 최신 콘텐츠 해시
	 * @return 콘텐츠가 변경되었으면 {@code true}
	 */
	public boolean isChanged(CrawledNotice notice, String latestContentHash) {
		return !notice.getContentHash().equals(latestContentHash);
	}
}
