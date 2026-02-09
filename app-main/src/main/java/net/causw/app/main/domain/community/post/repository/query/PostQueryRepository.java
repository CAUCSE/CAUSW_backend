package net.causw.app.main.domain.community.post.repository.query;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.QPostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FileExtensionType;
import net.causw.app.main.domain.community.comment.entity.QComment;
import net.causw.app.main.domain.community.post.entity.QPost;
import net.causw.app.main.domain.community.reaction.entity.QFavoritePost;
import net.causw.app.main.domain.community.reaction.entity.QLikePost;
import net.causw.app.main.domain.user.account.entity.user.QUser;

import com.querydsl.core.Tuple;
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
		Pageable pageable) {
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

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	/**
	 * 커서 기반 페이징으로 게시글 목록을 조회합니다. (V2용 - title 없음)
	 *
	 * @param boardIds 게시판 ID 목록 (null이면 전체 게시판, 빈 리스트면 조회 안함)
	 * @param cursorCreatedAt 커서 (마지막 게시글의 createdAt)
	 * @param cursorId 커서 (마지막 게시글의 ID, createdAt이 같을 때 사용)
	 * @param size 조회할 개수
	 * @param keyword 검색 키워드 (content 기준)
	 * @return 게시글 목록 Slice
	 */
	public Slice<PostCursorResult> findPostsWithCursor(
		List<String> boardIds,
		String cursorCreatedAt,
		String cursorId,
		int size,
		String keyword) {
		QPost post = QPost.post;
		QUser writer = new QUser("writer");

		// 커서 조건 (createdAt과 id 기반 커서 - 시간순 정렬 보장)
		BooleanExpression cursorCondition = NO_CONDITION;
		if (cursorCreatedAt != null && cursorId != null) {
			// createdAt이 cursor보다 이전이거나, createdAt이 같고 id가 cursor보다 작은 경우
			cursorCondition = post.createdAt.lt(java.time.LocalDateTime.parse(cursorCreatedAt))
				.or(post.createdAt.eq(java.time.LocalDateTime.parse(cursorCreatedAt)).and(post.id.lt(cursorId)));
		}

		// 게시판 조건
		BooleanExpression boardCondition = NO_CONDITION;
		if (boardIds != null && !boardIds.isEmpty()) {
			boardCondition = post.board.id.in(boardIds);
		}

		// 게시글 조회 조건
		BooleanExpression[] conditions = new BooleanExpression[] {
			boardCondition,
			post.isDeleted.eq(false),
			containsKeywordInContent(post, keyword),
			cursorCondition
		};

		// 게시글 조회 (size + 1개 조회하여 hasNext 판단)
		List<PostCursorResult> results = jpaQueryFactory
			.select(toPostCursorResult(post, writer))
			.from(post)
			.leftJoin(post.writer, writer)
			.where(conditions)
			.orderBy(post.createdAt.desc(), post.id.desc()) // createdAt 역순, id 역순
			.limit(size + 1)
			.fetch();

		// hasNext 판단 및 Slice 생성
		boolean hasNext = results.size() > size;
		List<PostCursorResult> content = hasNext ? results.subList(0, size) : results;

		return new SliceImpl<>(content, Pageable.ofSize(size), hasNext);
	}

	private BooleanExpression containsKeywordInContent(QPost post, String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return NO_CONDITION;
		}
		return post.content.contains(keyword);
	}

	private BooleanExpression isNotDeleted(QPost post, boolean includeDeleted) {
		return includeDeleted ? NO_CONDITION : post.isDeleted.eq(false);
	}

	private BooleanExpression notInBlockedUsers(QUser writer, Set<String> blockedUserIds) {
		return (blockedUserIds == null || blockedUserIds.isEmpty()) ? NO_CONDITION : writer.id.notIn(blockedUserIds);
	}

	private BooleanExpression containsKeyword(QPost post, QUser writer, String keyword) {
		if (keyword == null || keyword.isBlank())
			return NO_CONDITION;

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

		// 작성자 프로필 이미지 URL 서브쿼리
		SubQueryExpression<String> writerProfileImageUrl = JPAExpressions.select(
			writer.userProfileImage.uuidFile.fileUrl)
			.from(writer)
			.where(writer.eq(post.writer)
				.and(writer.userProfileImage.isNotNull()));

		// 썸네일 이미지 URL 서브쿼리 (첫 번째 이미지)
		SubQueryExpression<String> thumbnailUrl = JPAExpressions
			.select(postAttachImage.uuidFile.fileUrl)
			.from(postAttachImage)
			.where(postAttachImage.post.eq(post)
				.and(postAttachImage.uuidFile.extension.in(FileExtensionType.IMAGE.getExtensionList())))
			.orderBy(postAttachImage.uuidFile.createdAt.asc())
			.limit(1);

		return new QPostQueryResult(
			post.id, post.title, post.content,
			commentCount, likeCount, favoriteCount,
			post.isAnonymous, post.isQuestion, post.vote.id, post.isDeleted,
			writer.isNotNull(), writer.name, writer.nickname, writer.admissionYear, writer.state,
			writerProfileImageUrl,
			thumbnailUrl,
			post.vote.isNotNull(), // isPostVote
			post.form.isNotNull(), // isPostForm
			post.createdAt, post.updatedAt);
	}

	private static QPostCursorResult toPostCursorResult(QPost post, QUser writer) {

		QComment comment = QComment.comment;
		QLikePost likePost = QLikePost.likePost;
		QFavoritePost favoritePost = QFavoritePost.favoritePost;

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

		// 작성자 프로필 이미지 URL 서브쿼리
		SubQueryExpression<String> writerProfileImageUrl = JPAExpressions.select(
			writer.userProfileImage.uuidFile.fileUrl)
			.from(writer)
			.where(writer.eq(post.writer)
				.and(writer.userProfileImage.isNotNull()));

		return new QPostCursorResult(
			post.id, post.content,
			commentCount, likeCount, favoriteCount,
			post.isAnonymous, post.vote.id, post.isDeleted,
			writer.isNotNull(), writer.name, writer.nickname, writer.admissionYear, writer.state,
			writerProfileImageUrl,
			post.createdAt, post.updatedAt);
	}

	/**
	 * 특정 게시글들의 이미지 URL 목록을 조회합니다.
	 *
	 * @param postIds 게시글 ID 목록
	 * @return 게시글 ID를 키로, 이미지 URL 목록을 값으로 하는 맵
	 */
	public Map<String, List<String>> findPostImagesByPostIds(List<String> postIds) {
		QPostAttachImage postAttachImage = QPostAttachImage.postAttachImage;

		List<Tuple> results = jpaQueryFactory
			.select(postAttachImage.post.id, postAttachImage.uuidFile.fileUrl)
			.from(postAttachImage)
			.where(postAttachImage.post.id.in(postIds)
				.and(postAttachImage.uuidFile.extension.in(FileExtensionType.IMAGE.getExtensionList())))
			.orderBy(postAttachImage.post.id.asc(), postAttachImage.uuidFile.createdAt.asc())
			.fetch();

		return results.stream()
			.collect(Collectors.groupingBy(
				tuple -> tuple.get(postAttachImage.post.id),
				Collectors.mapping(
					tuple -> tuple.get(postAttachImage.uuidFile.fileUrl),
					Collectors.toList())));
	}
}
