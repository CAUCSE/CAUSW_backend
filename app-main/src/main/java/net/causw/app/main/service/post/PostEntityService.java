package net.causw.app.main.service.post;

import org.springframework.stereotype.Service;

import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostEntityService {
	private final PostRepository postRepository;

	public Post findById(String postId) {

		return postRepository.findById(postId).orElseThrow(() ->
			new NotFoundException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.VOTE_NOT_FOUND
			)
		);
	}
}
