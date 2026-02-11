package net.causw.app.main.domain.asset.file.service.v2.implementation;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.repository.PostAttachImageRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * PostAttachImage 도메인의 쓰기 작업을 담당하는 컴포넌트
 * Repository를 통해 데이터를 생성, 수정, 삭제합니다.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class PostAttachImageWriter {
	private final PostAttachImageRepository postAttachImageRepository;
	private final EntityManager entityManager;

	/**
	 * PostAttachImage 리스트를 삭제합니다.
	 *
	 * @param postAttachImages 삭제할 PostAttachImage 리스트
	 */
	public void deleteAll(List<PostAttachImage> postAttachImages) {
		postAttachImageRepository.deleteAll(postAttachImages);
	}

	/**
	 * PostAttachImage 리스트를 즉시 삭제합니다.
	 * 단일 DELETE 쿼리로 즉시 실행됩니다.
	 *
	 * @param postAttachImages 삭제할 PostAttachImage 리스트
	 */
	public void deleteAllInBatch(List<PostAttachImage> postAttachImages) {
		postAttachImageRepository.deleteAllInBatch(postAttachImages);
	}

	/**
	 * PostAttachImage를 삭제합니다.
	 *
	 * @param postAttachImage 삭제할 PostAttachImage
	 */
	public void delete(PostAttachImage postAttachImage) {
		postAttachImageRepository.delete(postAttachImage);
	}

	/**
	 * 영속성 컨텍스트의 변경 사항을 즉시 데이터베이스에 반영합니다.
	 */
	public void flush() {
		entityManager.flush();
	}

	/**
	 * 영속성 컨텍스트를 초기화합니다.
	 */
	public void clear() {
		entityManager.clear();
	}
}
