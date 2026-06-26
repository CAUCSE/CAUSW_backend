package net.causw.app.main.domain.community.comment.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.service.implementation.UserProfileImageReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.entity.LikeComment;
import net.causw.app.main.domain.community.comment.service.dto.CommentCreateCommand;
import net.causw.app.main.domain.community.comment.service.dto.CommentListQuery;
import net.causw.app.main.domain.community.comment.service.dto.CommentMeta;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentUpdateCommand;
import net.causw.app.main.domain.community.comment.service.implementation.CommentMapper;
import net.causw.app.main.domain.community.comment.service.implementation.CommentMetaReader;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.CommentWriter;
import net.causw.app.main.domain.community.comment.service.implementation.LikeCommentWriter;
import net.causw.app.main.domain.community.comment.util.CommentValidator;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.event.CommentChildCommentCreatedEvent;
import net.causw.app.main.domain.notification.notification.event.PostCommentCreatedEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 도메인의 비즈니스 로직을 처리합니다.
 *
 * <p>댓글 생성·수정·삭제·목록 조회 및 좋아요·좋아요 취소를 담당합니다.
 * 집계 데이터(좋아요 수, 차단 여부)는 {@link CommentMetaReader}를 통해 배치 또는 단건으로 조회하며,
 * 응답 객체 변환은 {@link CommentMapper}에 위임합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

	private final PostReader postReader;
	private final CommentReader commentReader;
	private final CommentWriter commentWriter;
	private final LikeCommentWriter likeCommentWriter;
	private final CommentValidator commentValidator;
	private final ApplicationEventPublisher eventPublisher;
	private final BlockReader blockReader;
	private final BoardConfigReader boardConfigReader;
	private final CommentMetaReader commentMetaReader;
	private final CommentMapper commentMapper;
	private final UserReader userReader;
	private final UserProfileImageReader userProfileImageReader;

	/**
	 * 댓글을 생성하고 응답 객체를 반환합니다.
	 *
	 * <p>생성 직후 게시글/댓글 작성자에게 알림을 발송합니다.
	 * 신규 댓글이므로 좋아요·대댓글이 없는 {@link CommentMeta#forNew()} 를 사용합니다.</p>
	 *
	 * @param command 댓글 생성 요청 데이터
	 * @return 생성된 댓글의 응답 객체
	 */
	@Transactional
	public CommentResult createComment(CommentCreateCommand command) {
		User creator = userReader.findUserByIdNotDeleted(command.creatorId());
		Comment parentComment = command.parentCommentId() == null
			? null
			: commentReader.findByIdAndNotDeleted(command.parentCommentId());
		Post post = parentComment == null
			? postReader.findById(command.postId())
			: postReader.findById(parentComment.getPost().getId());
		Comment comment = parentComment == null
			? Comment.ofRoot(command.content(), command.isAnonymous(), creator, post)
			: Comment.ofReply(command.content(), command.isAnonymous(), creator, parentComment);

		commentValidator.validateForCreate(creator, post);
		commentValidator.validateReplyDepth(parentComment);
		commentWriter.save(comment);

		// 신규 댓글: 좋아요 0, 대댓글 없음
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		Map<String, UserProfileImage> profileImageMap = userProfileImageReader.findMapByUserIds(
			List.of(creator.getId()));
		CommentResult result = commentMapper.toResult(comment, creator, boardAdminIds, CommentMeta.forNew(),
			profileImageMap);

		if (parentComment == null) {
			eventPublisher.publishEvent(new PostCommentCreatedEvent(post.getId(), comment.getId()));
		} else {
			eventPublisher.publishEvent(new CommentChildCommentCreatedEvent(parentComment.getId(), comment.getId()));
		}

		return result;
	}

	/**
	 * 게시글에 속한 댓글 목록을 페이지 단위로 조회합니다.
	 *
	 * <p>댓글이 없으면 빈 페이지를 즉시 반환합니다(Early Exit).
	 * N+1 방지를 위해 좋아요·차단 집계 데이터를 {@link CommentMetaReader#fetch}로 배치 조회한 뒤
	 * 각 댓글에 매핑합니다.</p>
	 *
	 * @param query 댓글 목록 조회 쿼리 데이터
	 * @return 댓글 응답 페이지
	 */
	public Page<CommentResult> findAllComments(CommentListQuery query) {
		User viewer = userReader.findUserByIdNotDeleted(query.viewerId());
		Post post = postReader.findById(query.postId());

		commentValidator.validateForFind(viewer, post);

		Page<Comment> comments = commentReader.getComments(query.postId(), query.pageable());
		if (comments.getContent().isEmpty())
			return Page.empty(query.pageable());

		// 게시판 권한 및 차단 정보 조회
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		Set<String> blockedUserIds = blockReader.findBlockeeUserIdsByBlocker(viewer);

		// 댓글·대댓글 집계 데이터 배치 조회
		Map<String, CommentMeta> metaMap = commentMetaReader.fetch(viewer.getId(), blockedUserIds,
			comments.getContent());

		// 댓글·대댓글 작성자 프로필 이미지 일괄 조회
		List<String> writerIds = comments.getContent().stream()
			.flatMap(c -> {
				List<String> ids = new ArrayList<>();
				if (c.getWriter() != null)
					ids.add(c.getWriter().getId());
				c.getChildCommentList().forEach(child -> {
					if (child.getWriter() != null)
						ids.add(child.getWriter().getId());
				});
				return ids.stream();
			})
			.distinct()
			.collect(Collectors.toList());
		Map<String, UserProfileImage> profileImageMap = userProfileImageReader.findMapByUserIds(writerIds);

		return comments.map(c -> commentMapper.toResult(c, viewer, boardAdminIds, metaMap.get(c.getId()),
			profileImageMap));
	}

	/**
	 * 댓글 내용을 수정하고 응답 객체를 반환합니다.
	 *
	 * @param command 댓글 수정 요청 데이터
	 * @return 수정된 댓글의 응답 객체
	 */
	@Transactional
	public CommentResult updateComment(CommentUpdateCommand command) {
		User updater = userReader.findUserByIdNotDeleted(command.updaterId());
		Comment comment = commentReader.getComment(command.commentId());
		Post post = postReader.findById(comment.getPost().getId());

		commentValidator.validateForUpdate(updater, post, comment);
		comment.update(command.content());
		commentWriter.save(comment);

		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		CommentMeta meta = commentMetaReader.fetchForComment(updater, comment, Set.of());
		Map<String, UserProfileImage> profileImageMap = userProfileImageReader.findMapByUserIds(
			collectCommentWriterIds(comment));
		return commentMapper.toResult(comment, updater, boardAdminIds, meta, profileImageMap);
	}

	/**
	 * 댓글을 소프트 삭제하고 응답 객체를 반환합니다.
	 *
	 * @param deleterId 삭제 요청 유저 ID
	 * @param commentId 삭제할 댓글 ID
	 * @return 삭제된 댓글의 응답 객체
	 */
	@Transactional
	public CommentResult deleteComment(String deleterId, String commentId) {
		User deleter = userReader.findUserByIdNotDeleted(deleterId);
		Comment comment = commentReader.getComment(commentId);
		Post post = postReader.findById(comment.getPost().getId());

		commentValidator.validateForDelete(deleter, post, comment);
		comment.delete();
		commentWriter.save(comment);

		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		CommentMeta meta = commentMetaReader.fetchForComment(deleter, comment, Set.of());
		Map<String, UserProfileImage> profileImageMap = userProfileImageReader.findMapByUserIds(
			collectCommentWriterIds(comment));
		return commentMapper.toResult(comment, deleter, boardAdminIds, meta, profileImageMap);
	}

	/**
	 * 댓글에 좋아요를 추가합니다.
	 *
	 * @param userId    좋아요를 누를 유저 ID
	 * @param commentId 좋아요를 누를 댓글 ID
	 */
	@Transactional
	public void likeComment(String userId, String commentId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		Comment comment = commentReader.getComment(commentId);

		commentValidator.validateForLike(user, comment);

		LikeComment likeComment = LikeComment.of(comment, user);
		likeCommentWriter.save(likeComment);
	}

	/**
	 * 댓글 좋아요를 취소합니다.
	 *
	 * @param userId    좋아요를 취소할 유저 ID
	 * @param commentId 좋아요를 취소할 댓글 ID
	 */
	@Transactional
	public void cancelLikeComment(String userId, String commentId) {
		User user = userReader.findUserByIdNotDeleted(userId);
		Comment comment = commentReader.getComment(commentId);

		commentValidator.validateForCancelLike(user, comment);

		likeCommentWriter.delete(commentId, user.getId());
	}

	/** 댓글과 그 대댓글의 작성자 ID를 중복 없이 수집합니다. */
	private List<String> collectCommentWriterIds(Comment comment) {
		List<String> ids = new ArrayList<>();
		if (comment.getWriter() != null)
			ids.add(comment.getWriter().getId());
		comment.getChildCommentList().forEach(child -> {
			if (child.getWriter() != null)
				ids.add(child.getWriter().getId());
		});
		return ids.stream().distinct().collect(Collectors.toList());
	}

}
