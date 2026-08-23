package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.integration.crawled.entity.CrawledPostImage;
import net.causw.app.main.domain.integration.crawled.repository.CrawledPostImageRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class CrawledPostImageWriter {
	private final CrawledPostImageRepository crawledPostImageRepository;

	public void deleteAllByPostId(String postId) {
		crawledPostImageRepository.deleteAllByPostId(postId);
	}

	public void saveAll(List<CrawledPostImage> images) {
		crawledPostImageRepository.saveAll(images);
	}
}
