package net.causw.app.main.repository.post.query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.comment.QComment;
import net.causw.app.main.domain.model.entity.post.QFavoritePost;
import net.causw.app.main.domain.model.entity.post.QLikePost;
import net.causw.app.main.domain.model.entity.post.QPost;
import net.causw.app.main.domain.model.entity.user.QUser;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.QPostAttachImage;
import net.causw.app.main.domain.model.enums.uuidFile.FileExtensionType;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

	private static final BooleanExpression NO_CONDITION = null;

	private final JPAQueryFactory jpaQueryFactory;

	public Page<PostQueryResult> findPostsByBoardWithFilters(
		String boardId,
		boolean includeDeleted,
		Set<String> blockedUserIds,
		String keyword,
		Pageable pageable
	) {
		QPost post = QPost.post;
		QUser writer = new QUser("writer");

		// 게시글 조회 조건
		BooleanExpression[] conditions = new BooleanExpression[] {
			post.board.id.eq(boardId),
			isNotDeleted(post, includeDeleted),
			notInBlockedUsers(writer, blockedUserIds),
			containsKeyword(post, writer, keyword)
		};

		// 게시글 조회
		List<PostQueryResult> content = jpaQueryFactory
			.select(toPostQueryResult(post, writer))
			.from(post)
			.leftJoin(post.writer, writer)
			.where(conditions)
			.orderBy(post.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		// 게시글 개수 조회
		Long total = jpaQueryFactory
			.select(post.count())
			.from(post)
			.leftJoin(post.writer, writer)
			.where(conditions)
			.fetchOne();

		return new PageImpl<>(content, pageable, Optional.ofNullable(total).orElse(0L));
	}

	private BooleanExpression isNotDeleted(QPost post, boolean includeDeleted) {
		return includeDeleted ? NO_CONDITION : post.isDeleted.eq(false);
	}

	private BooleanExpression notInBlockedUsers(QUser writer, Set<String> blockedUserIds) {
		return (blockedUserIds == null || blockedUserIds.isEmpty()) ? NO_CONDITION : writer.id.notIn(blockedUserIds);
	}

	private BooleanExpression containsKeyword(QPost post, QUser writer, String keyword) {
		if (keyword == null || keyword.isBlank()) return NO_CONDITION;

		return post.title.contains(keyword) // MySQL에서 utf8mb4_0900_ai_ci Collation 사용중이므로, 기본적으로 대소문자 무시
			.or(post.content.contains(keyword))
			.or(writer.nickname.contains(keyword));
	}

	private static QPostQueryResult toPostQueryResult(QPost post, QUser writer) {

		QComment comment = QComment.comment;
		QLikePost likePost = QLikePost.likePost;
		QFavoritePost favoritePost = QFavoritePost.favoritePost;
		QPostAttachImage postAttachImage = QPostAttachImage.postAttachImage;

		// 숫자 카운트 서브쿼리
		SubQueryExpression<Long> commentCount = JPAExpressions
			.select(comment.count())
			.from(comment)
			.where(comment.post.eq(post));

		SubQueryExpression<Long> likeCount = JPAExpressions
			.select(likePost.count())
			.from(likePost)
			.where(likePost.post.eq(post));

		SubQueryExpression<Long> favoriteCount = JPAExpressions
			.select(favoritePost.count())
			.from(favoritePost)
			.where(favoritePost.post.eq(post));

		// 문자열 서브쿼리 (썸네일 URL)
		SubQueryExpression<String> thumbnailUrl = JPAExpressions.select(postAttachImage.uuidFile.fileUrl)
			.from(postAttachImage)
			.where(postAttachImage.post.eq(post)
				.and(postAttachImage.uuidFile.extension.in(FileExtensionType.IMAGE.getExtensionList()))
				.and(postAttachImage.uuidFile.createdAt.eq(
					JPAExpressions.select(postAttachImage.uuidFile.createdAt.min())
						.from(postAttachImage)
						.where(postAttachImage.post.eq(post))
				)));

		return new QPostQueryResult(
			post.id, post.title, post.content,
			commentCount, likeCount, favoriteCount,
			post.isAnonymous, post.isQuestion, post.vote.isNotNull(), post.form.isNotNull(), post.isDeleted,
			writer.isNotNull(), writer.name, writer.nickname, writer.admissionYear, writer.state,
			post.createdAt, post.updatedAt,
			thumbnailUrl
		);
	}
}
