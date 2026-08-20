package net.causw.app.main.domain.integration.crawled.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@Transactional
/**
 * 정제된 크롤링 공지를 일괄적으로 영속화하는 서비스입니다.
 *
 * <p>기존 공지와 연관 첨부파일을 한 번에 조회한 뒤 같은 트랜잭션에서 생성 또는 갱신하여,
 * 지연 로딩 예외와 공지별 재조회에 따른 N+1 쿼리를 방지합니다.</p>
 */
public class CrawledNoticePersistenceService {
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeWriter crawledNoticeWriter;
	private final PostWriter postWriter;

	/**
	 * 사이트 내 공지 목록을 일괄 생성 또는 갱신합니다.
	 *
	 * @param siteId 크롤링 출처 사이트 식별자
	 * @param articles 저장할 정제 공지 목록
	 * @return 외부 공지 식별자별 저장 결과
	 */
	public Map<String, CrawlSaveStatus> persistAll(String siteId, List<CleanArticle> articles) {
		if (articles.isEmpty()) {
			return Map.of();
		}

		Map<String, CrawledNotice> noticesByExternalId = crawledNoticeReader.findBySources(
			siteId, articles.stream().map(CleanArticle::externalId).toList());
		return articles.stream().collect(Collectors.toMap(
			CleanArticle::externalId,
			article -> persist(article, noticesByExternalId.get(article.externalId())),
			(existing, replacement) -> replacement));
	}

	private CrawlSaveStatus persist(CleanArticle article, CrawledNotice existing) {
		if (existing != null && !existing.getTargetBoardId().equals(article.targetBoardId())) {
			softDeleteLinkedPost(existing);
		}
		return crawledNoticeWriter.upsert(article, existing);
	}

	private void softDeleteLinkedPost(CrawledNotice notice) {
		Post post = notice.getPost();
		if (post != null && !Boolean.TRUE.equals(post.getIsDeleted())) {
			post.setIsDeleted(true);
			postWriter.save(post);
		}
	}
}
