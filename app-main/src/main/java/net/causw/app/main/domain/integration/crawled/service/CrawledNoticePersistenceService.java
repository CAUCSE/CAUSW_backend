package net.causw.app.main.domain.integration.crawled.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.integration.crawled.dto.CleanArticle;
import net.causw.app.main.domain.integration.crawled.dto.CrawlSaveStatus;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeContentHashManager;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * 정제된 크롤링 공지를 일괄적으로 영속화하는 서비스입니다.
 *
 * <p>기존 공지와 연관 첨부파일을 한 번에 조회해 공지별 재조회에 따른 N+1 쿼리를 방지합니다.</p>
 */
public class CrawledNoticePersistenceService {
	private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeContentHashManager crawledNoticeContentHashManager;
	private final CrawledNoticeWriter crawledNoticeWriter;
	private final PostWriter postWriter;
	private final TransactionTemplate transactionTemplate;

	public CrawledNoticePersistenceService(
		CrawledNoticeReader crawledNoticeReader,
		CrawledNoticeContentHashManager crawledNoticeContentHashManager,
		CrawledNoticeWriter crawledNoticeWriter,
		PostWriter postWriter,
		PlatformTransactionManager transactionManager) {
		this.crawledNoticeReader = crawledNoticeReader;
		this.crawledNoticeContentHashManager = crawledNoticeContentHashManager;
		this.crawledNoticeWriter = crawledNoticeWriter;
		this.postWriter = postWriter;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	/**
	 * 사이트 내 공지 목록을 일괄 생성 또는 갱신합니다.
	 *
	 * @param siteConfig 크롤링 출처와 최대 스캔 범위를 포함한 사이트 설정
	 * @param articles 저장할 정제 공지 목록
	 * @return 외부 공지 식별자별 저장 결과
	 */
	public Map<String, CrawlSaveStatus> persistAll(SiteConfig siteConfig, List<CleanArticle> articles) {
		if (articles.isEmpty()) {
			return Map.of();
		}

		// 공지별 조회를 피하기 위해 이번 크롤링 대상의 기존 공지를 한 번에 조회합니다.
		Map<String, CrawledNotice> noticesByExternalId = crawledNoticeReader.findBySources(
			siteConfig.getSiteId(), articles.stream().map(CleanArticle::externalId).toList());
		Map<String, CrawlSaveStatus> saveStatuses = new HashMap<>();
		for (CleanArticle article : articles) {
			try {
				// 공지 한 건마다 독립 트랜잭션을 열어, 저장 실패가 다음 공지 처리에 영향을 주지 않게 합니다.
				CrawlSaveStatus saveStatus = transactionTemplate
					.execute(status -> persist(article, noticesByExternalId.get(article.externalId()), siteConfig));
				saveStatuses.put(article.externalId(), saveStatus);
			} catch (RuntimeException e) {
				// 저장 실패는 수집 실패 URL과 구분해 로그로만 남기고 다음 공지를 계속 처리합니다.
				log.error("[크롤링] 공지 저장 실패. siteId={}, externalId={}, sourceUrl={}",
					siteConfig.getSiteId(), article.externalId(), article.sourceUrl(), e);
			}
		}
		return saveStatuses;
	}

	private CrawlSaveStatus persist(CleanArticle article, CrawledNotice existing, SiteConfig siteConfig) {
		if (existing == null) {
			// 신규 공지는 최근 공지만 저장해 오래된 공지의 Post 전송 대기를 만들지 않습니다.
			if (!isWithinScanRange(article.announceDate(), siteConfig)) {
				return CrawlSaveStatus.SKIPPED;
			}
			crawledNoticeWriter.save(article);
			return CrawlSaveStatus.CREATED;
		}

		boolean targetBoardChanged = existing.isTargetBoardChanged(article.targetBoardId());
		if (targetBoardChanged) {
			// 게시판이 변경되면 이전 게시판에 연결된 Post를 더 이상 노출하지 않습니다.
			softDeleteLinkedPost(existing);
		}
		if (!targetBoardChanged && !crawledNoticeContentHashManager.isChanged(existing, article.contentHash())) {
			// 내용과 저장 대상이 모두 같으면 DB 변경 및 Post 재전송을 생략합니다.
			return CrawlSaveStatus.UNCHANGED;
		}

		// 내용 또는 저장 대상이 변경된 공지는 다음 Post 전송 대상으로 표시합니다.
		crawledNoticeWriter.update(existing, article);
		return CrawlSaveStatus.UPDATED;
	}

	private boolean isWithinScanRange(LocalDate announceDate, SiteConfig siteConfig) {
		LocalDate today = LocalDate.now(KOREA_ZONE_ID);
		LocalDate oldestAllowedDate = today.minusDays(siteConfig.getMaxScanRangeDays());
		return !announceDate.isBefore(oldestAllowedDate) && !announceDate.isAfter(today);
	}

	private void softDeleteLinkedPost(CrawledNotice notice) {
		Post post = notice.getPost();
		if (post != null && !Boolean.TRUE.equals(post.getIsDeleted())) {
			post.setIsDeleted(true);
			postWriter.save(post);
		}
	}
}
