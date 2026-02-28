package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.shared.exception.errorcode.CommentErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 댓글 엔티티 조회를 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class CommentReader {

	private final CommentRepository commentRepository;
	private final ChildCommentReader childCommentReader;

	/**
	 * ID로 댓글을 조회합니다. 존재하지 않으면 예외를 발생시킵니다.
	 *
	 * @param commentId 조회할 댓글 ID
	 * @return 조회된 {@link Comment} 엔티티
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 댓글이 존재하지 않는 경우
	 */
	public Comment getComment(String commentId) {
		return commentRepository.findById(commentId).orElseThrow(
			CommentErrorCode.COMMENT_NOT_FOUND::toBaseException);
	}

	/**
	 * 게시글에 속한 댓글 목록을 페이지 단위로 조회합니다.
	 *
	 * <p>N+1 문제를 방지하기 위해 대댓글을 부모 댓글 ID 목록 기준으로 일괄(batch) 조회한 뒤,
	 * 각 댓글의 {@code childCommentList}에 주입합니다.</p>
	 *
	 * @param postId   댓글을 조회할 게시글 ID
	 * @param pageable 페이지 요청 정보
	 * @return 대댓글이 채워진 댓글 페이지
	 */
	public Page<Comment> getComments(String postId, Pageable pageable) {
		Page<Comment> comments = commentRepository.findByPost_IdOrderByCreatedAt(postId, pageable);
		List<String> commentIds = comments.getContent().stream().map(Comment::getId).toList();

		if (!commentIds.isEmpty()) {
			List<ChildComment> allChildComments = childCommentReader.getChildCommentsByParentIds(commentIds);

			Map<String, List<ChildComment>> childCommentMap = allChildComments.stream()
				.collect(Collectors.groupingBy(child -> child.getParentComment().getId()));

			comments.forEach(comment -> comment
				.setChildCommentList(childCommentMap.getOrDefault(comment.getId(), Collections.emptyList())));
		}
		return comments;
	}

}
