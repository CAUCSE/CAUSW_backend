package net.causw.app.main.domain.community.comment.service.dto;

import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.user.account.entity.user.User;

/**
 * 댓글 목록 조회 쿼리 데이터.
 *
 * <p>API 레이어에서 변환되어 {@link net.causw.app.main.domain.community.comment.service.CommentService#findAllComments}로 전달됩니다.</p>
 *
 * @param viewer   댓글 목록을 조회하는 유저
 * @param postId   댓글 목록을 조회할 게시글 ID
 * @param pageable 페이지 요청 정보
 */
public record CommentListQuery(
	User viewer,
	String postId,
	Pageable pageable) {
}
