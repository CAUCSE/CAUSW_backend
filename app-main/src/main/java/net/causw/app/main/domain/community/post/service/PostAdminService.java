package net.causw.app.main.domain.community.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.community.post.service.dto.UncategorizedPostResult;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostAdminService {

	private final PostReader postReader;

	/**
	 * 관리자가 게시글의 성격(카테고리)을 수동으로 지정합니다.
	 *
	 * <p>자동 분류 결과를 덮어쓰며 변경 이력은 남기지 않습니다.</p>
	 *
	 * @param postId 수정할 게시글 식별자
	 * @param category 지정할 성격. null이면 미분류
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 크롤링 게시글이 아닌 경우
	 */
	@Transactional
	public void updateCategory(String postId, PostCategory category) {
		Post post = postReader.findByIdAndNotDeleted(postId);

		if (!Boolean.TRUE.equals(post.getIsCrawled())) {
			throw PostErrorCode.POST_CATEGORY_NOT_SUPPORTED.toBaseException();
		}

		post.updateCategory(category);
	}

	/**
	 * 성격이 미분류인 크롤링 게시글을 조회합니다.
	 *
	 * @param pageable 페이지 정보
	 * @return 미분류 게시글 페이지
	 */
	@Transactional(readOnly = true)
	public Page<UncategorizedPostResult> getUncategorizedPosts(Pageable pageable) {
		return postReader.findUncategorizedCrawledPosts(pageable)
			.map(post -> new UncategorizedPostResult(
				post.getId(),
				post.getTitle(),
				post.getBoard().getName(),
				post.getCreatedAt()));
	}
}
