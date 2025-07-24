package net.causw.app.main.service.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;
import net.causw.app.main.repository.crawled.CrawledNoticeRepository;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingAndSavingService {
	private final Crawler crawler;
	private final CrawledNoticeRepository crawledNoticeRepository;

	// 크롤링 및 공지사항 수정, 추가 감지
	@Transactional
	public void crawlAndDetectUpdates() {
		try {
			List<CrawledNotice> notices = crawler.crawl();
			if (notices.isEmpty()) {
				return;
			}

			List<CrawledNotice> noticesToSave = processLatestNotices(notices);
			if (!noticesToSave.isEmpty()) {
				crawledNoticeRepository.saveAll(noticesToSave);
			}
		} catch (Exception e) {
			log.error("크롤링 중 오류 발생", e);
		}
	}

	//최신 30개 공지에 대해서만 업데이트 감지 및 저장
	private List<CrawledNotice> processLatestNotices(List<CrawledNotice> notices) {
		List<CrawledNotice> noticesToSave = new ArrayList<>();
		List<CrawledNotice> latestNotices = notices.stream()
			.sorted((a, b) -> b.getAnnounceDate().compareTo(a.getAnnounceDate()))
			.limit(StaticValue.CRAWLING_MAX_NOTICES)
			.toList();

		for (CrawledNotice notice : latestNotices) {
			Optional<CrawledNotice> existingOpt = crawledNoticeRepository.findByLink(notice.getLink());

			if (existingOpt.isPresent()) {
				CrawledNotice existing = existingOpt.get();

				if (!existing.getContentHash().equals(notice.getContentHash())) {
					existing.updateContent(notice.getTitle(), notice.getContent(), notice.getContentHash());
					noticesToSave.add(existing);
				}
			} else {
				notice.setIsUpdated(true);
				noticesToSave.add(notice);
			}
		}

		return noticesToSave;
	}
} 