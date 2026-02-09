package net.causw.app.main.domain.community.post.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostReader {

	private final PostRepository postRepository;

	/**
	 * 게시글 식별자를 통해 게시글 엔티티를 조회합니다.
	 *
	 * @param postId 조회하고자 하는 게시글의 고유 식별자
	 * @return 조회된 Post 엔티티
	 */
	public Post getPost(String postId) {
		return postRepository.findById(postId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.POST_NOT_FOUND));
	}

}
