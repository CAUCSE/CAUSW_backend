package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrawledNoticeReader {
	private final CrawledNoticeRepository crawledNoticeRepository;

	/**
	 * 출처 사이트와 사이트 내부 식별자로 저장된 공지를 조회합니다.
	 *
	 * @param siteId 출처 사이트 식별자
	 * @param externalId 사이트 내부 공지 식별자
	 * @return 저장된 공지
	 */
	public Optional<CrawledNotice> findBySource(String siteId, String externalId) {
		return crawledNoticeRepository.findBySiteIdAndExternalId(siteId, externalId);
	}

	/**
	 * Post 생성 또는 갱신을 기다리는 최근 공지를 조회합니다.
	 *
	 * @return 전송 대기 공지 목록
	 */
	public List<CrawledNotice> findPendingNotices() {
		return crawledNoticeRepository.findTop30ByIsUpdatedTrueOrderByLastModifiedDesc();
	}
}
