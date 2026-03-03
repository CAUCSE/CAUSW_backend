package net.causw.app.main.domain.community.post.repository.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.asset.file.entity.joinEntity.QPostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FileExtensionType;
import net.causw.app.main.domain.community.comment.entity.QChildComment;
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
		BooleanExpression cursorCondition = createCursorCondition(cursorCreatedAt, cursorId, post);

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
		return getPostCursorResults(size, post, writer, conditions);
	}

	/**
	 * 특정 사용자가 댓글을 작성한 게시글을 커서 기반 페이징으로 조회합니다.
	 * findPostsWithCursor와 동일하게 PostCursorResult를 반환하여 재사용합니다.
	 *
	 * @param userId         댓글 작성자 ID
	 * @param blockedUserIds 차단한 사용자 ID 목록 (해당 사용자 게시글 제외)
	 * @param cursorCreatedAt 커서 (마지막 게시글의 createdAt)
	 * @param cursorId       커서 (마지막 게시글의 ID)
	 * @param size           조회할 개수
	 * @return 게시글 목록 Slice
	 */
	public Slice<PostCursorResult> findPostsCommentedByUserWithCursor(
		String userId,
		Set<String> blockedUserIds,
		String cursorCreatedAt,
		String cursorId,
		int size) {
		QPost post = QPost.post;
		QUser writer = new QUser("writer");
		QComment comment = QComment.comment;

		// 커서 조건
		BooleanExpression cursorCondition = createCursorCondition(cursorCreatedAt, cursorId, post);

		// 해당 사용자가 댓글을 단 글이 존재하는지
		BooleanExpression userCommentedPost = JPAExpressions
			.selectOne()
			.from(comment)
			.where(
				comment.post.eq(post),
				comment.writer.id.eq(userId),
				comment.isDeleted.isFalse())
			.exists();

		BooleanExpression[] conditions = new BooleanExpression[] {
			userCommentedPost, // 댓글 단 글만 조회
			writer.id.ne(userId), // 자신이 쓴 글은 제외
			post.isDeleted.eq(false), // 삭제된 글 제외
			notInBlockedUsers(writer, blockedUserIds), // 차단한 사용자 글 제외
			cursorCondition // 커서 조건
		};

		return getPostCursorResults(size, post, writer, conditions);
	}

	/**
	 * 특정 사용자가 작성한 게시글을 커서 기반 페이징으로 조회합니다.
	 * @param userId 작성자 ID
	 * @param cursorCreatedAt 커서 (마지막 게시글의 createdAt)
	 * @param cursorId 커서 (마지막 게시글의 ID)
	 * @param size 조회할 개수
	 * @return
	 */
	public Slice<PostCursorResult> findPostsWrittenByUserWithCursor(
		String userId,
		String cursorCreatedAt,
		String cursorId,
		int size) {
		QPost post = QPost.post;
		QUser writer = new QUser("writer");

		BooleanExpression cursorCondition = createCursorCondition(cursorCreatedAt, cursorId, post);

		BooleanExpression[] conditions = new BooleanExpression[] {
			post.writer.id.eq(userId), // 자신이 쓴 글만 조회
			post.isDeleted.eq(false), // 삭제된 글 제외
			cursorCondition // 커서 조건
		};

		return getPostCursorResults(size, post, writer, conditions);
	}

	/**
	 * 특정 사용자가 좋아요를 누른 게시글을 커서 기반 페이징으로 조회합니다.
	 * @param userId 좋아요 누른 사용자 ID
	 * @param blockedUserIds 차단한 사용자 ID 목록 (해당 사용자 게시글 제외)
	 * @param cursorCreatedAt 커서 (마지막 게시글의 createdAt)
	 * @param cursorId 커서 (마지막 게시글의 ID)
	 * @param size 조회할 개수
	 * @return 게시글 목록 Slice
	 */
	public Slice<PostCursorResult> findPostsLikedByUserWithCursor(
		String userId,
		Set<String> blockedUserIds,
		String cursorCreatedAt,
		String cursorId,
		int size) {
		QPost post = QPost.post;
		QUser writer = new QUser("writer");
		QLikePost likePost = QLikePost.likePost;

		BooleanExpression cursorCondition = createCursorCondition(cursorCreatedAt, cursorId, post);

		BooleanExpression userLikedPost = JPAExpressions
			.selectOne()
			.from(likePost)
			.where(likePost.post.eq(post), likePost.user.id.eq(userId))
			.exists();

		BooleanExpression[] conditions = new BooleanExpression[] {
			userLikedPost, // 좋아요 누른 글만 조회
			post.isDeleted.eq(false), // 삭제된 글 제외
			notInBlockedUsers(writer, blockedUserIds), // 차단한 사용자 글 제외
			cursorCondition
		};

		return getPostCursorResults(size, post, writer, conditions);
	}

	/**
	 * 커서 기반 페이징에서 다음 페이지 조회를 위한 조건을 생성합니다.
	 * @param cursorCreatedAt 마지막으로 조회된 게시글의 createdAt (ISO_LOCAL_DATE_TIME 형식)
	 * @param cursorId 마지막으로 조회된 게시글의 ID (createdAt이 같은 경우, ID로 추가 정렬하여 다음 페이지 조회)
	 * @param post QPost 엔티티의 Q타입
	 * @return 커서 조건 (createdAt과 ID 기반) 또는 null (커서 정보가 없는 경우)
	 */
	@Nullable
	private static BooleanExpression createCursorCondition(String cursorCreatedAt, String cursorId, QPost post) {
		BooleanExpression cursorCondition = NO_CONDITION;
		if (cursorCreatedAt != null && cursorId != null) {
			cursorCondition = post.createdAt.lt(LocalDateTime.parse(cursorCreatedAt))
				.or(post.createdAt.eq(LocalDateTime.parse(cursorCreatedAt)).and(post.id.lt(cursorId)));
		}
		return cursorCondition;
	}

	/**
	 * 주어진 조건으로 게시글을 조회하여 PostCursorResult 리스트를 반환합니다. (커서 기반 페이징용)
	 * @param size 조회할 개수 (hasNext 판단 위해 실제 조회는 size + 1)
	 * @param post QPost 엔티티의 Q타입
	 * @param writer QUser 엔티티의 Q타입 (작성자 정보 조인용)
	 * @param conditions 조회 조건 배열 (null 또는 빈 배열이면 조건 없이 조회)
	 * @return 조회된 게시글 목록과 다음 페이지 존재 여부를 포함하는 Slice<PostCursorResult>
	 */
	@NotNull
	private Slice<PostCursorResult> getPostCursorResults(
		int size,
		QPost post,
		QUser writer,
		BooleanExpression[] conditions) {
		List<PostCursorResult> results = jpaQueryFactory
			.select(toPostCursorResult(post, writer))
			.from(post)
			.leftJoin(post.writer, writer)
			.where(conditions)
			.orderBy(post.createdAt.desc(), post.id.desc())
			.limit(size + 1)
			.fetch();

		boolean hasNext = results.size() > size;
		List<PostCursorResult> content = hasNext ? results.subList(0, size) : results;
		return new SliceImpl<>(content, Pageable.ofSize(size), hasNext);
	}

	/**
	 * 게시글 내용에 키워드가 포함되어 있는지 여부를 반환하는 조건식을 생성합니다.
	 * @param post QPost 엔티티의 Q타입
	 * @param keyword 검색 키워드 (null 또는 빈 문자열이면 조건 없이 조회)
	 * @return 게시글 내용에 키워드가 포함되어 있는지 여부를 나타내는 BooleanExpression 또는 null (키워드가 없는 경우)
	 */
	private BooleanExpression containsKeywordInContent(QPost post, String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return NO_CONDITION;
		}
		return post.content.contains(keyword);
	}

	/**
	 * 게시글이 삭제되지 않은 경우에만 조회하도록 하는 조건식을 생성합니다.
	 * @param post QPost 엔티티의 Q타입
	 * @param includeDeleted 삭제된 게시글도 포함할지 여부 (true면 삭제된 게시글도 조회, false면 삭제된 게시글 제외)
	 * @return 게시글이 삭제되지 않은 경우에만 조회하도록 하는 BooleanExpression 또는 null (includeDeleted가 true인 경우)
	 */
	private BooleanExpression isNotDeleted(QPost post, boolean includeDeleted) {
		return includeDeleted ? NO_CONDITION : post.isDeleted.eq(false);
	}

	/**
	 * 작성자가 차단된 사용자 목록에 포함되지 않은 경우에만 조회하도록 하는 조건식을 생성합니다.
	 * @param writer QUser 엔티티의 Q타입 (작성자 정보 조인용)
	 * @param blockedUserIds 차단된 사용자 ID 목록 (null이면 차단된 사용자 없음, 빈 리스트면 차단된 사용자 없음)
	 * @return 작성자가 차단된 사용자 목록에 포함되지 않은 경우에만 조회하도록 하는 BooleanExpression 또는 null (차단된 사용자 목록이 없는 경우)
	 */
	private BooleanExpression notInBlockedUsers(QUser writer, Set<String> blockedUserIds) {
		return (blockedUserIds == null || blockedUserIds.isEmpty()) ? NO_CONDITION : writer.id.notIn(blockedUserIds);
	}

	/**
	 * 게시글 제목, 내용, 작성자 닉네임 중 하나라도 검색 키워드를 포함하는지 여부를 반환하는 조건식을 생성합니다.
	 * @param post QPost 엔티티의 Q타입
	 * @param writer QUser 엔티티의 Q타입 (작성자 정보 조인용)
	 * @param keyword 검색 키워드 (null 또는 빈 문자열이면 조건 없이 조회)
	 * @return 게시글 제목, 내용, 작성자 닉네임 중 하나라도 검색 키워드를 포함하는지 여부를 나타내는 BooleanExpression 또는 null (키워드가 없는 경우)
	 */
	private BooleanExpression containsKeyword(QPost post, QUser writer, String keyword) {
		if (keyword == null || keyword.isBlank())
			return NO_CONDITION;

		return post.title.contains(keyword) // MySQL에서 utf8mb4_0900_ai_ci Collation 사용중이므로, 기본적으로 대소문자 무시
			.or(post.content.contains(keyword))
			.or(writer.nickname.contains(keyword));
	}

	/**
	 * 게시글과 작성자 정보를 기반으로 PostQueryResult를 생성하는 팩토리 메서드입니다.
	 * @param post QPost 엔티티의 Q타입
	 * @param writer QUser 엔티티의 Q타입 (작성자 정보 조인용)
	 * @return 게시글과 작성자 정보를 포함하는 PostQueryResult 객체
	 */
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

		// 좋아요 개수 서브쿼리
		SubQueryExpression<Long> likeCount = JPAExpressions
			.select(likePost.count())
			.from(likePost)
			.where(likePost.post.eq(post));

		// 즐겨찾기 개수 서브쿼리
		SubQueryExpression<Long> favoriteCount = JPAExpressions
			.select(favoritePost.count())
			.from(favoritePost)
			.where(favoritePost.post.eq(post));

		// 문자열 서브쿼리 (썸네일 URL)
		SubQueryExpression<String> thumbnailUrl = JPAExpressions.select(
			postAttachImage.uuidFile.fileUrl)
			.from(postAttachImage)
			.where(postAttachImage.post.eq(post)
				.and(postAttachImage.uuidFile.extension.in(
					FileExtensionType.IMAGE.getExtensionList()))
				.and(postAttachImage.uuidFile.createdAt.eq(
					JPAExpressions.select(postAttachImage.uuidFile.createdAt.min())
						.from(postAttachImage)
						.where(postAttachImage.post.eq(post)))));

		return new QPostQueryResult(
			post.id, post.title, post.content,
			commentCount, likeCount, favoriteCount,
			post.isAnonymous, post.isQuestion, post.vote.isNotNull(), post.form.isNotNull(),
			post.isDeleted,
			writer.isNotNull(), writer.name, writer.nickname, writer.admissionYear, writer.state, writer.deletedAt,
			post.createdAt, post.updatedAt,
			thumbnailUrl);
	}

	/**
	 * 게시글과 작성자 정보를 기반으로 PostCursorResult를 생성하는 팩토리 메서드입니다. (V2용 - title 없음)
	 * @param post QPost 엔티티의 Q타입
	 * @param writer QUser 엔티티의 Q타입 (작성자 정보 조인용)
	 * @return 게시글과 작성자 정보를 포함하는 PostCursorResult 객체
	 */
	private static QPostCursorResult toPostCursorResult(QPost post, QUser writer) {

		QComment comment = QComment.comment;
		QChildComment childComment = QChildComment.childComment;
		QLikePost likePost = QLikePost.likePost;
		QFavoritePost favoritePost = QFavoritePost.favoritePost;

		// Comment 개수 + ChildComment 개수 (삭제되지 않은 것만)
		SubQueryExpression<Long> totalCommentCount = JPAExpressions
			.select(comment.count()
				.add(JPAExpressions
					.select(childComment.count())
					.from(childComment)
					.where(childComment.parentComment.post.eq(post)
						.and(childComment.isDeleted.isFalse()))))
			.from(comment)
			.where(comment.post.eq(post).and(comment.isDeleted.isFalse()));

		// 좋아요 개수 서브쿼리
		SubQueryExpression<Long> likeCount = JPAExpressions
			.select(likePost.count())
			.from(likePost)
			.where(likePost.post.eq(post));

		// 즐겨찾기 개수 서브쿼리
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
			totalCommentCount, likeCount, favoriteCount,
			post.isAnonymous, post.vote.id, post.isDeleted,
			writer.isNotNull(), writer.name, writer.nickname, writer.admissionYear, writer.state,
			writerProfileImageUrl,
			post.createdAt, post.updatedAt,
			post.board.id, post.board.name);
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

	/**
	 * 특정 게시글의 댓글 개수를 조회합니다. (Comment + ChildComment, 삭제되지 않은 것만)
	 *
	 * @param postId 게시글 ID
	 * @return 댓글 개수 (Comment 개수 + ChildComment 개수)
	 */
	public long countCommentsByPostId(String postId) {
		QComment comment = QComment.comment;
		QChildComment childComment = QChildComment.childComment;

		// Comment 개수 조회 (삭제되지 않은 것만)
		Long commentCount = jpaQueryFactory
			.select(comment.count())
			.from(comment)
			.where(comment.post.id.eq(postId)
				.and(comment.isDeleted.isFalse()))
			.fetchOne();

		// ChildComment 개수 조회 (삭제되지 않은 것만)
		Long childCommentCount = jpaQueryFactory
			.select(childComment.count())
			.from(childComment)
			.where(childComment.parentComment.post.id.eq(postId)
				.and(childComment.isDeleted.isFalse()))
			.fetchOne();

		return (commentCount != null ? commentCount : 0L) + (childCommentCount != null ? childCommentCount : 0L);
	}
}
