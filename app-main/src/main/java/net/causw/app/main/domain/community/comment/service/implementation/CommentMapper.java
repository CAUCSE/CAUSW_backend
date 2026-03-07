package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentAuthorInfo;
import net.causw.app.main.domain.community.comment.service.dto.CommentMeta;
import net.causw.app.main.domain.community.comment.service.dto.CommentResult;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

/**
 * {@link Comment} 엔티티를 {@link CommentResult} 응답 객체로 변환합니다.
 *
 * <p>변환에 필요한 집계 데이터(좋아요 수, 차단 여부 등)는 {@link CommentMeta}를 통해 주입받으며,
 * 대댓글 변환은 {@link ChildCommentMapper}에 위임합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class CommentMapper {

	private final ChildCommentMapper childCommentMapper;

	/**
	 * 댓글 엔티티와 집계 메타 데이터를 조합하여 {@link CommentResult}를 생성합니다.
	 *
	 * <p>작성자가 차단된 경우 {@code content}를 {@code null}로 설정합니다.
	 * 대댓글 목록은 {@code meta}의 자식 좋아요 수·차단 정보를 활용해 함께 변환됩니다.</p>
	 *
	 * @param comment      변환할 댓글 엔티티
	 * @param user         현재 조회 유저
	 * @param boardAdminIds 게시판 관리자 ID 목록 (수정·삭제 권한 계산에 사용)
	 * @param meta         이 댓글의 집계 메타 데이터
	 * @return 변환된 {@code CommentResult}
	 */
	public CommentResult toResult(Comment comment, User user, List<String> boardAdminIds, CommentMeta meta) {
		List<ChildCommentResult> childResults = comment.getChildCommentList().stream()
			.map(child -> childCommentMapper.toResult(child, user,
				boardAdminIds,
				meta.childLikeCounts().getOrDefault(child.getId(), 0L),
				meta.likedChildIds().contains(child.getId()),
				meta.blockedChildIds().contains(child.getId())))
			.toList();

		CommentAuthorInfo authorInfo = CommentAuthorInfo.of(
			comment.getWriter(), comment.getIsAnonymous(), user,
			boardAdminIds, meta.isBlocked());

		String content = meta.isBlocked() ? null : comment.getContent();

		return new CommentResult(
			comment.getId(),
			content,
			comment.getCreatedAt(),
			comment.getUpdatedAt(),
			comment.getPost().getId(),
			authorInfo,
			meta.isLiked(),
			meta.isSubscribed(),
			meta.numLike(),
			(long)comment.getChildCommentList().size(),
			childResults);
	}

}
