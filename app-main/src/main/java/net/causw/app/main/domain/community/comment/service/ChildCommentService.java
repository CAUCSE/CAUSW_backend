package net.causw.app.main.domain.community.comment.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.entity.LikeChildComment;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentCreateCommand;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentMeta;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentResult;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentUpdateCommand;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentMapper;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentWriter;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.LikeChildCommentWriter;
import net.causw.app.main.domain.community.comment.util.ChildCommentValidator;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.service.v2.event.CommentChildCommentCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

/**
 * 대댓글 도메인의 비즈니스 로직을 처리합니다.
 *
 * <p>대댓글 생성·수정·삭제 및 좋아요·좋아요 취소를 담당합니다.
 * 응답 객체 변환은 {@link ChildCommentMapper}에 위임합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class ChildCommentService {

	private final CommentReader commentReader;
	private final ChildCommentReader childCommentReader;
	private final PostReader postReader;
	private final ChildCommentWriter childCommentWriter;
	private final LikeChildCommentWriter likeChildCommentWriter;
	private final ChildCommentValidator childCommentValidator;
	private final ApplicationEventPublisher eventPublisher;
	private final BoardConfigReader boardConfigReader;
	private final LikeChildCommentReader likeChildCommentReader;
	private final ChildCommentMapper childCommentMapper;
	private final UserReader userReader;

	/**
	 * 대댓글을 생성하고 응답 객체를 반환합니다.
	 *
	 * <p>생성 직후 부모 댓글 구독자에게 알림을 발송합니다.
	 * 신규 대댓글이므로 좋아요 0으로 초기화된 {@link ChildCommentMeta}를 사용합니다.</p>
	 *
	 * @param command 대댓글 생성 요청 데이터
	 * @return 생성된 대댓글의 응답 객체
	 */
	@Transactional
	public ChildCommentResult createChildComment(ChildCommentCreateCommand command) {
		User creator = userReader.findUserByIdNotDeleted(command.creatorId());
		Comment parentComment = commentReader.getComment(command.parentCommentId());
		Post post = postReader.findById(parentComment.getPost().getId());
		ChildComment childComment = ChildComment.of(
			command.content(),
			false,
			command.isAnonymous(),
			creator,
			parentComment);

		childCommentValidator.validateForCreate(creator, post, parentComment);
		childCommentWriter.save(childComment);

		// 신규 대댓글: 좋아요 0
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		ChildCommentResult result = childCommentMapper.toResult(
			childComment, creator, new ChildCommentMeta(boardAdminIds, 0L, false, false));

		eventPublisher.publishEvent(new CommentChildCommentCreatedEvent(parentComment, childComment));

		return result;
	}

	/**
	 * 대댓글 내용을 수정하고 응답 객체를 반환합니다.
	 *
	 * @param command 대댓글 수정 요청 데이터
	 * @return 수정된 대댓글의 응답 객체
	 */
	@Transactional
	public ChildCommentResult updateChildComment(ChildCommentUpdateCommand command) {
		User updater = userReader.findUserByIdNotDeleted(command.updaterId());
		ChildComment childComment = childCommentReader.findById(command.childCommentId());
		Post post = postReader.findById(childComment.getParentComment().getPost().getId());

		childComment.update(command.content());
		childCommentValidator.validateForUpdate(updater, post, childComment);
		childCommentWriter.save(childComment);

		// 응답에 필요한 데이터 조회
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		long numLike = likeChildCommentReader.getNumOfChildCommentLikes(childComment);
		boolean isLiked = likeChildCommentReader.isChildCommentLiked(updater, childComment.getId());

		return childCommentMapper.toResult(
			childComment, updater, new ChildCommentMeta(boardAdminIds, numLike, isLiked, false));
	}

	/**
	 * 대댓글을 소프트 삭제하고 응답 객체를 반환합니다.
	 *
	 * @param deleterId      삭제 요청 유저 ID
	 * @param childCommentId 삭제할 대댓글 ID
	 * @return 삭제된 대댓글의 응답 객체
	 */
	@Transactional
	public ChildCommentResult deleteChildComment(String deleterId, String childCommentId) {
		User deleter = userReader.findUserByIdNotDeleted(deleterId);
		ChildComment childComment = childCommentReader.findById(childCommentId);
		Post post = postReader.findById(childComment.getParentComment().getPost().getId());

		childCommentValidator.validateForDelete(deleter, post, childComment);
		childComment.delete();
		childCommentWriter.save(childComment);

		// 응답에 필요한 데이터 조회
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		long numLike = likeChildCommentReader.getNumOfChildCommentLikes(childComment);
		boolean isLiked = likeChildCommentReader.isChildCommentLiked(deleter, childComment.getId());

		return childCommentMapper.toResult(
			childComment, deleter, new ChildCommentMeta(boardAdminIds, numLike, isLiked, false));
	}

	/**
	 * 대댓글에 좋아요를 추가합니다.
	 *
	 * @param userId         좋아요를 누를 유저 ID
	 * @param childCommentId 좋아요를 누를 대댓글 ID
	 */
	@Transactional
	public void likeChildComment(String userId, String childCommentId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		ChildComment childComment = childCommentReader.findById(childCommentId);

		childCommentValidator.validateForLike(user, childComment);

		LikeChildComment likeChildComment = LikeChildComment.of(childComment, user);
		likeChildCommentWriter.save(likeChildComment);
	}

	/**
	 * 대댓글 좋아요를 취소합니다.
	 *
	 * @param userId         좋아요를 취소할 유저 ID
	 * @param childCommentId 좋아요를 취소할 대댓글 ID
	 */
	@Transactional
	public void cancelLikeChildComment(String userId, String childCommentId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		ChildComment childComment = childCommentReader.findById(childCommentId);

		childCommentValidator.validateForCancelLike(user, childComment);

		likeChildCommentWriter.delete(childCommentId, user.getId());
	}

}
