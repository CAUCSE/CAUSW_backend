package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentMeta;
import net.causw.app.main.domain.community.comment.service.dto.ChildCommentResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentAuthorInfo;
import net.causw.app.main.domain.user.account.entity.user.User;

/**
 * {@link ChildComment} 엔티티를 {@link ChildCommentResult} 응답 객체로 변환합니다.
 *
 * <p>변환에 필요한 집계 데이터는 {@link ChildCommentMeta} 또는 개별 파라미터로 주입받습니다.
 * 프로필 이미지는 호출자가 일괄 조회한 {@code profileImageMap}을 통해 주입받습니다.</p>
 */
@Component
public class ChildCommentMapper {

	/**
	 * 대댓글 엔티티와 렌더 데이터를 조합하여 {@link ChildCommentResult}를 생성합니다.
	 *
	 * <p>작성자가 차단된 경우 {@code content}를 {@code null}로 설정합니다.</p>
	 *
	 * @param childComment    변환할 대댓글 엔티티
	 * @param user            현재 조회 유저
	 * @param data            대댓글 렌더링에 필요한 집계 데이터
	 * @param profileImageMap 호출자가 일괄 조회한 유저 ID → 프로필 이미지 맵
	 * @return 변환된 {@code ChildCommentResult}
	 */
	public ChildCommentResult toResult(ChildComment childComment, User user, ChildCommentMeta data,
		Map<String, UserProfileImage> profileImageMap) {

		// 삭제된 대댓글이면 익명처리
		Boolean isAnonymous = childComment.getIsDeleted() ? Boolean.TRUE : childComment.getIsAnonymous();

		UserProfileImage writerProfileImage = (childComment.getWriter() != null)
			? profileImageMap.get(childComment.getWriter().getId())
			: null;
		CommentAuthorInfo authorInfo = CommentAuthorInfo.of(
			childComment.getWriter(), writerProfileImage, isAnonymous, user,
			data.boardAdminIds(), data.isBlocked());

		// 차단 대댓글이거나 삭제된 대댓글의 경우 content를 null로 처리
		String content = (data.isBlocked() || childComment.getIsDeleted()) ? null : childComment.getContent();

		return new ChildCommentResult(
			childComment.getId(),
			content,
			childComment.getIsDeleted(),
			childComment.getCreatedAt(),
			childComment.getUpdatedAt(),
			authorInfo,
			data.isLiked(),
			data.numLike());
	}

	/**
	 * 대댓글 렌더링에 필요한 개별 값을 받아 {@link ChildCommentResult}를 생성하는 편의 메서드.
	 *
	 * <p>댓글 목록 조회 시 {@link CommentMapper}에서 대댓글을 변환할 때 사용됩니다.</p>
	 *
	 * @param childComment    변환할 대댓글 엔티티
	 * @param user            현재 조회 유저
	 * @param boardAdminIds   게시판 관리자 ID 목록
	 * @param numLike         이 대댓글의 좋아요 수
	 * @param isLiked         현재 조회 유저의 좋아요 여부
	 * @param isBlocked       작성자 차단 여부
	 * @param profileImageMap 호출자가 일괄 조회한 유저 ID → 프로필 이미지 맵
	 * @return 변환된 {@code ChildCommentResult}
	 */
	public ChildCommentResult toResult(ChildComment childComment, User user,
		List<String> boardAdminIds, long numLike, boolean isLiked, boolean isBlocked,
		Map<String, UserProfileImage> profileImageMap) {
		return toResult(childComment, user, new ChildCommentMeta(boardAdminIds, numLike, isLiked, isBlocked),
			profileImageMap);
	}

}
