package net.causw.app.main.domain.community.comment.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.QLikeComment;
import net.causw.app.main.domain.community.comment.repository.query.CommentLikeCountDto;
import net.causw.app.main.domain.community.comment.repository.query.QCommentLikeCountDto;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LikeCommentQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 댓글 ID 목록에 대한 좋아요 수를 계산합니다.
	 * @param commentIds 좋아요 수를 계산할 댓글 ID 목록
	 * @return 각 댓글 ID와 해당 댓글에 대한 좋아요 수를 포함하는 DTO 목록
	 */
	public List<CommentLikeCountDto> countLikesByCommentIds(List<String> commentIds) {
		QLikeComment likeComment = QLikeComment.likeComment;

		return jpaQueryFactory
			.select(getCommentLikeCountDto(likeComment))
			.from(likeComment)
			.where(likeComment.comment.id.in(commentIds))
			.groupBy(likeComment.comment.id)
			.fetch();
	}

	private static QCommentLikeCountDto getCommentLikeCountDto(QLikeComment likeComment) {
		return new QCommentLikeCountDto(
			likeComment.comment.id,
			likeComment.count());
	}
}
