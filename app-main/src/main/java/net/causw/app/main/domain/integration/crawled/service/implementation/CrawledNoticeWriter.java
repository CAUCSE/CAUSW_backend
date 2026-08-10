package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CleanAttachment;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.entity.CrawledFileLink;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class CrawledNoticeWriter {
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeRepository crawledNoticeRepository;

	public CrawlSaveStatus upsert(CleanArticle article) {
		return crawledNoticeReader.findBySource(article.siteId(), article.externalId())
			.map(existing -> updateIfChanged(existing, article))
			.orElseGet(() -> create(article));
	}

	public void markTransferred(CrawledNotice notice, Post post) {
		notice.linkPost(post);
		notice.setIsUpdated(false);
		crawledNoticeRepository.save(notice);
	}

	private CrawlSaveStatus create(CleanArticle article) {
		CrawledNotice notice = CrawledNotice.of(
			article.siteId(),
			article.externalId(),
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
		return CrawlSaveStatus.CREATED;
	}

	private CrawlSaveStatus updateIfChanged(CrawledNotice existing, CleanArticle article) {
		if (existing.getContentHash().equals(article.contentHash())) {
			return CrawlSaveStatus.UNCHANGED;
		}
		existing.updateFrom(
			article.category(),
			article.title(),
			article.contentHtml(),
			article.sourceUrl(),
			article.author(),
			article.announceDate(),
			article.representativeImageUrl(),
			toEntities(article.attachments()),
			article.contentHash());
		return CrawlSaveStatus.UPDATED;
	}

	private List<CrawledFileLink> toEntities(List<CleanAttachment> attachments) {
		return attachments.stream()
			.map(attachment -> CrawledFileLink.of(attachment.fileName(), attachment.fileUrl()))
			.toList();
	}
}
