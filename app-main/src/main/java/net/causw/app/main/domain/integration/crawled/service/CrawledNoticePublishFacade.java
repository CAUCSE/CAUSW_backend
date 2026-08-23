package net.causw.app.main.domain.integration.crawled.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 전송 대기 공지를 Post로 변환하는 배치 흐름을 조율하는 파사드입니다.
 *
 * <p>목록 조회와 항목별 실패 격리를 담당하며, 개별 공지의 Post 생성 또는 갱신은
 * {@link CrawledNoticeTransferService}에 위임합니다.</p>
 */
public class CrawledNoticePublishFacade {
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeTransferService crawledNoticeTransferService;

	/**
	 * 전송 대기 중인 크롤링 공지를 대상 게시판의 Post로 생성하거나 갱신합니다.
	 *
	 * <p>한 공지의 전송이 실패해도 나머지 공지 전송을 계속 수행합니다.</p>
	 */
	public void publishPendingNotices() {
		List<CrawledNotice> updatedNotices = crawledNoticeReader.findPendingNotices();
		int savedCount = 0;
		for (CrawledNotice notice : updatedNotices) {
			try {
				crawledNoticeTransferService.transfer(notice.getId());
				savedCount++;
			} catch (RuntimeException e) {
				log.error("[크롤링] 공지 Post 변환 실패. noticeId={}, siteId={}, externalId={}",
					notice.getId(), notice.getSiteId(), notice.getExternalId(), e);
			}
		}
		log.info("[크롤링] 공지 Post 변환 완료. 변환 수={}", savedCount);
	}
}
