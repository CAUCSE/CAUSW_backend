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

	public Optional<CrawledNotice> findBySource(String siteId, String externalId) {
		return crawledNoticeRepository.findBySiteIdAndExternalId(siteId, externalId);
	}

	public List<CrawledNotice> findPendingNotices() {
		return crawledNoticeRepository.findTop30ByIsUpdatedTrueOrderByLastModifiedDesc();
	}
}
