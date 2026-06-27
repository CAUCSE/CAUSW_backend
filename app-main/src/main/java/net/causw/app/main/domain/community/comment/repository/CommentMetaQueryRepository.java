package net.causw.app.main.domain.community.comment.repository;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.QComment;
import net.causw.app.main.domain.community.comment.entity.QLikeComment;
import net.causw.app.main.domain.community.comment.repository.query.CommentMetaQueryResult;
import net.causw.app.main.domain.community.comment.repository.query.QCommentMetaQueryResult;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentMetaQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 댓글 ID 목록에 대한 렌더링 메타 데이터를 한 번에 조회합니다.
	 *
	 * @param userId 현재 조회하는 유저 ID
	 * @param blockedUserIds 현재 유저가 차단한 유저 ID 집합
	 * @param commentIds 조회할 댓글 ID 목록
	 * @return 댓글별 좋아요 수, 좋아요 여부, 차단 여부를 포함하는 DTO 목록
	 */
	public List<CommentMetaQueryResult> findCommentMetaByCommentIds(
		String userId,
		Set<String> blockedUserIds,
		List<String> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return List.of();
		}

		QComment comment = QComment.comment;
		QLikeComment likeComment = QLikeComment.likeComment;
		QLikeComment userLikeComment = new QLikeComment("userLikeComment");

		return jpaQueryFactory
			.select(new QCommentMetaQueryResult(
				comment.id,
				comment.parentComment.id,
				likeComment.id.countDistinct(),
				userLikeComment.id.count().gt(0),
				isBlocked(comment, blockedUserIds)))
			.from(comment)
			.leftJoin(likeComment).on(likeComment.comment.eq(comment))
			.leftJoin(userLikeComment).on(userLikeComment.comment.eq(comment)
				.and(userLikeComment.user.id.eq(userId)))
			.where(comment.id.in(commentIds))
			.groupBy(comment.id, comment.parentComment.id, comment.writer.id)
			.fetch();
	}

	private BooleanExpression isBlocked(QComment comment, Set<String> blockedUserIds) {
		if (blockedUserIds == null || blockedUserIds.isEmpty()) {
			return Expressions.FALSE.isTrue();
		}
		return comment.writer.id.in(blockedUserIds);
	}
}
