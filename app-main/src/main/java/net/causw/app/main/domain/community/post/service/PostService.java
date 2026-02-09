package net.causw.app.main.domain.community.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.LikePostReader;
import net.causw.app.main.domain.community.post.service.implementation.LikePostWriter;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.post.util.PostValidator;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostReader postReader;
	private final LikePostReader likePostReader;
	private final LikePostWriter likePostWriter;
	private final PostValidator postValidator;

	/**
	 * 게시글 좋아요 메서드
	 * @param userId 좋아요 누른 유저 id
	 * @param postId 좋아요 누른 게시글 아이디
	 */
	@Transactional
	public void likePost(String userId, String postId) {
		Post post = postReader.getPost(postId);

		postValidator.validateWriterNotDeleted(post);

		if (likePostReader.isPostLiked(userId, postId)) {
			throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.POST_ALREADY_LIKED);
		}

		likePostWriter.saveLikePost(userId, post);
	}

	/**
	 * 게시글 좋아요 취소 메서드
	 * @param userId 좋아요 취소 누른 유저 id
	 * @param postId 좋아요 취소 누른 게시글 아이디
	 */
	@Transactional
	public void cancelLikePost(final String userId, final String postId) {
		Post post = postReader.getPost(postId);

		postValidator.validateWriterNotDeleted(post);

		if (!likePostReader.isPostLiked(userId, postId)) {
			throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.POST_NOT_LIKED);
		}

		likePostWriter.deleteLikePost(postId, userId);
	}
}
