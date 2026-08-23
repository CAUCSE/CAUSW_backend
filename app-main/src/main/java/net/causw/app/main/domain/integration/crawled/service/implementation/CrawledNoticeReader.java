package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrawledNoticeReader {
	private final CrawledNoticeRepository crawledNoticeRepository;

	/**
	 * 한 사이트의 외부 식별자 목록으로 저장된 공지를 일괄 조회합니다.
	 *
	 * @param siteId 출처 사이트 식별자
	 * @param externalIds 사이트 내부 공지 식별자 목록
	 * @return 외부 식별자를 키로 하는 저장된 공지 맵
	 */
	public Map<String, CrawledNotice> findBySources(String siteId, List<String> externalIds) {
		return crawledNoticeRepository.findBySiteIdAndExternalIdIn(siteId, externalIds).stream()
			.collect(Collectors.toMap(CrawledNotice::getExternalId, Function.identity()));
	}

	public CrawledNotice findById(String noticeId) {
		return crawledNoticeRepository.findById(noticeId)
			.orElseThrow(IntegrationErrorCode.CRAWLING_ERROR::toBaseException);
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
