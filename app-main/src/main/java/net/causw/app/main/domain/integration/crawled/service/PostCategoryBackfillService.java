package net.causw.app.main.domain.integration.crawled.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.community.post.util.PostCategoryClassifier;
import net.causw.app.main.domain.integration.crawled.repository.query.PostCategoryBackfillTarget;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 성격이 미분류로 남아 있는 기존 크롤링 게시글을 소급 분류합니다.
 *
 * <p>신규 전환과 같은 분류기를 사용하며, 이미 성격이 지정된 게시글은 대상에서 제외됩니다.
 * Post를 새로 만들지 않으므로 공지 알림은 발행되지 않습니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostCategoryBackfillService {

	private static final int CHUNK_SIZE = 200;

	private final CrawledNoticeReader crawledNoticeReader;
	private final PostCategoryClassifier postCategoryClassifier;
	private final PostWriter postWriter;

	/**
	 * 미분류 크롤링 게시글을 청크 단위로 조회해 성격을 채웁니다.
	 *
	 * @return 성격이 새로 지정된 게시글 수
	 */
	@Transactional
	public int backfill() {
		int updated = 0;
		String cursor = "";

		while (true) {
			List<PostCategoryBackfillTarget> targets = crawledNoticeReader.findCategoryBackfillTargets(cursor,
				CHUNK_SIZE);
			if (targets.isEmpty()) {
				break;
			}

			for (Map.Entry<PostCategory, List<String>> entry : groupByCategory(targets).entrySet()) {
				updated += postWriter.updateCategoryByIds(entry.getKey(), entry.getValue());
			}

			cursor = targets.get(targets.size() - 1).noticeId();
		}

		log.info("[크롤링] 게시글 성격 백필 완료. 분류 수={}", updated);
		return updated;
	}

	// 분류에 실패한 대상은 미분류로 남기기 위해 결과에서 제외한다.
	private Map<PostCategory, List<String>> groupByCategory(List<PostCategoryBackfillTarget> targets) {
		Map<PostCategory, List<String>> grouped = new EnumMap<>(PostCategory.class);
		for (PostCategoryBackfillTarget target : targets) {
			PostCategory category = postCategoryClassifier.classify(target.title());
			if (category == null) {
				continue;
			}
			grouped.computeIfAbsent(category, key -> new ArrayList<>()).add(target.postId());
		}
		return grouped;
	}
}
