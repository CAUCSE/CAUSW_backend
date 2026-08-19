package net.causw.app.main.domain.integration.crawled.service;

import java.util.List;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeTransferProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawledToPostTransferService {
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeTransferProcessor crawledNoticeTransferProcessor;

	/**
	 * 전송 대기 중인 크롤링 공지를 대상 게시판의 Post로 생성하거나 갱신합니다.
	 */
	public void transferToPosts() {
		List<CrawledNotice> updatedNotices = crawledNoticeReader.findPendingNotices();
		int savedCount = 0;
		for (CrawledNotice notice : updatedNotices) {
			try {
				crawledNoticeTransferProcessor.transfer(notice.getId());
				savedCount++;
			} catch (RuntimeException e) {
				log.error("[크롤링] 공지 Post 변환 실패. noticeId={}, siteId={}, externalId={}",
					notice.getId(), notice.getSiteId(), notice.getExternalId(), e);
			}
		}
		log.info("[크롤링] 공지 Post 변환 완료. 변환 수={}", savedCount);
	}
}
