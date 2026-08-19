package net.causw.app.main.domain.integration.crawled.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrawledNoticeUpsertService {
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeWriter crawledNoticeWriter;
	private final PostWriter postWriter;

	/**
	 * 출처 식별자를 기준으로 공지를 저장하고, 대상 게시판이 변경되면 연결된 Post를 소프트 삭제합니다.
	 *
	 * @param article 저장할 정제 공지
	 * @return 생성, 수정 또는 미변경 상태
	 */
	@Transactional
	public CrawlSaveStatus upsert(CleanArticle article) {
		crawledNoticeReader.findBySource(article.siteId(), article.externalId())
			.filter(notice -> !notice.getTargetBoardId().equals(article.targetBoardId()))
			.ifPresent(this::softDeleteLinkedPost);
		return crawledNoticeWriter.upsert(article);
	}

	private void softDeleteLinkedPost(CrawledNotice notice) {
		Post post = notice.getPost();
		if (post != null && !Boolean.TRUE.equals(post.getIsDeleted())) {
			post.setIsDeleted(true);
			postWriter.save(post);
		}
	}
}
