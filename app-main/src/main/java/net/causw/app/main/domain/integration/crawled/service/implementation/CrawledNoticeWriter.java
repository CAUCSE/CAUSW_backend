package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CleanAttachment;
import net.causw.app.main.domain.integration.crawled.entity.CrawledFileLink;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class CrawledNoticeWriter {
	private final CrawledNoticeRepository crawledNoticeRepository;

	/**
	 * 공지에 변환된 Post를 연결하고 전송 완료 상태로 저장합니다.
	 *
	 * @param notice 전송을 완료한 공지
	 * @param post 생성하거나 갱신한 Post
	 */
	public void markTransferred(CrawledNotice notice, Post post) {
		notice.linkPost(post);
		notice.setIsUpdated(false);
		crawledNoticeRepository.save(notice);
	}

	/**
	 * 정제 공지를 새 엔티티로 저장하고 Post 전송 대기 상태로 표시합니다.
	 *
	 * @param article 저장할 정제 공지
	 */
	public void save(CleanArticle article) {
		CrawledNotice notice = CrawledNotice.of(
			article.siteId(),
			article.externalId(),
			article.targetBoardId(),
			article.category(),
			article.title(),
			article.contentHtml(),
			article.sourceUrl(),
			article.author(),
			article.announceDate(),
			article.representativeImageUrl(),
			toEntities(article.attachments()),
			article.contentHash());
		notice.setIsUpdated(true);
		crawledNoticeRepository.save(notice);
	}

	/**
	 * 기존 공지를 최신 정제 공지 내용으로 갱신합니다.
	 *
	 * @param existing 저장된 공지
	 * @param article 최신 정제 공지
	 */
	public void update(CrawledNotice existing, CleanArticle article) {
		existing.updateFrom(
			article.targetBoardId(),
			article.category(),
			article.title(),
			article.contentHtml(),
			article.sourceUrl(),
			article.author(),
			article.announceDate(),
			article.representativeImageUrl(),
			toEntities(article.attachments()),
			article.contentHash());
		crawledNoticeRepository.save(existing);
	}

	/**
	 * 정제된 첨부파일 DTO를 영속화할 엔티티 목록으로 변환합니다.
	 *
	 * @param attachments 정제된 첨부파일 목록
	 * @return 첨부파일 엔티티 목록
	 */
	private List<CrawledFileLink> toEntities(List<CleanAttachment> attachments) {
		return attachments.stream()
			.map(attachment -> CrawledFileLink.of(attachment.fileName(), attachment.fileUrl()))
			.toList();
	}
}
