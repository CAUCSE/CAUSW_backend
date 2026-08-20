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
	private final CrawledNoticeRepository crawledNoticeRepository;

	/**
	 * 출처 식별자를 기준으로 공지를 생성하거나 변경된 내용을 갱신합니다.
	 *
	 * @param article 저장할 정제 공지
	 * @param existing 출처 식별자로 미리 조회한 기존 공지. 없으면 새 공지를 생성한다.
	 * @return 생성, 수정 또는 미변경 상태
	 */
	public CrawlSaveStatus upsert(CleanArticle article, CrawledNotice existing) {
		if (existing == null) {
			return create(article);
		}
		return updateIfChanged(existing, article);
	}

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
	 * @return 생성 상태
	 */
	private CrawlSaveStatus create(CleanArticle article) {
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
		return CrawlSaveStatus.CREATED;
	}

	/**
	 * 콘텐츠 또는 저장 대상 게시판이 변경된 경우 기존 공지를 갱신합니다.
	 *
	 * @param existing 저장된 공지
	 * @param article 최신 정제 공지
	 * @return 수정 또는 미변경 상태
	 */
	private CrawlSaveStatus updateIfChanged(CrawledNotice existing, CleanArticle article) {
		boolean targetBoardChanged = !existing.getTargetBoardId().equals(article.targetBoardId());
		if (existing.getContentHash().equals(article.contentHash()) && !targetBoardChanged) {
			return CrawlSaveStatus.UNCHANGED;
		}
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
		return CrawlSaveStatus.UPDATED;
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
