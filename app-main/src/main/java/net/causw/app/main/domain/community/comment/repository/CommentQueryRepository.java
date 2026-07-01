package net.causw.app.main.domain.community.comment.repository;

import static net.causw.app.main.domain.community.comment.entity.QComment.comment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.Comment;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * post에 대한 루트 comments 찾기
	 * @param postId post id
	 * @param pageable
	 * @return comment 페이지
	 */
	public Page<Comment> findRootCommentsByPostId(String postId, Pageable pageable) {
		List<Comment> content = jpaQueryFactory
			.selectFrom(comment)
			.leftJoin(comment.writer).fetchJoin()
			.where(
				comment.post.id.eq(postId),
				comment.parentComment.isNull(),
				isNotDeleted())
			.orderBy(comment.createdAt.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		JPAQuery<Long> countQuery = jpaQueryFactory
			.select(comment.count())
			.from(comment)
			.where(
				comment.post.id.eq(postId),
				comment.parentComment.isNull(),
				isNotDeleted());

		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}

	/**
	 * 특정 comment 들에 대한 childComments find
	 * @param parentCommentIds 부모 댓글 id
	 * @return comment
	 */
	public List<Comment> findChildCommentsByParentCommentIds(List<String> parentCommentIds) {
		if (parentCommentIds == null || parentCommentIds.isEmpty()) {
			return List.of();
		}

		return jpaQueryFactory
			.selectFrom(comment)
			.leftJoin(comment.writer).fetchJoin()
			.leftJoin(comment.parentComment).fetchJoin()
			.leftJoin(comment.post).fetchJoin()
			.where(
				comment.parentComment.id.in(parentCommentIds),
				isNotDeleted())
			.orderBy(comment.createdAt.asc())
			.fetch();
	}

	private BooleanExpression isNotDeleted() {
		return comment.isDeleted.isFalse().or(comment.isDeleted.isNull());
	}
}
