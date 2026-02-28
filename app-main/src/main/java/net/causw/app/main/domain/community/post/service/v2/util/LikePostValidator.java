package net.causw.app.main.domain.community.post.service.v2.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.shared.exception.errorcode.LikePostErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikePostValidator {

	private final LikePostReader likePostReader;

	public void validateForLike(String userId, String postId) {
		if (likePostReader.existsByPostIdAndUserId(userId, postId)) {
			throw LikePostErrorCode.POST_ALREADY_LIKED.toBaseException();
		}
	}

	public void validateForCancelLike(String userId, String postId) {
		if (!likePostReader.existsByPostIdAndUserId(userId, postId)) {
			throw LikePostErrorCode.POST_NOT_LIKE.toBaseException();
		}
	}
}
