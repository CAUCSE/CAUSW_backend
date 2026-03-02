package net.causw.app.main.domain.community.comment.repository;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.community.comment.entity.QLikeChildComment;
import net.causw.app.main.domain.community.comment.repository.query.ChildCommentLikeCountDto;
import net.causw.app.main.domain.community.comment.repository.query.QChildCommentLikeCountDto;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LikeChildCommentQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	/**
	 * 자식 댓글 ID 목록에 대한 좋아요 수를 계산합니다.
	 * @param childCommentIds 좋아요 수를 계산할 자식 댓글 ID 목록
	 * @return 각 자식 댓글 ID와 해당 댓글에 대한 좋아요 수를 포함하는 DTO 목록
	 */
	public List<ChildCommentLikeCountDto> countLikesByChildCommentIds(List<String> childCommentIds) {
		QLikeChildComment likeChildComment = QLikeChildComment.likeChildComment;

		return jpaQueryFactory
			.select(getChildCommentLikeCountDto(likeChildComment))
			.from(likeChildComment)
			.where(likeChildComment.childComment.id.in(childCommentIds))
			.groupBy(likeChildComment.childComment.id)
			.fetch();
	}

	@NotNull
	private static QChildCommentLikeCountDto getChildCommentLikeCountDto(QLikeChildComment likeChildComment) {
		return new QChildCommentLikeCountDto(
			likeChildComment.childComment.id,
			likeChildComment.count());
	}
}
