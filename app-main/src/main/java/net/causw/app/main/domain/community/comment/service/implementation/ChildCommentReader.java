package net.causw.app.main.domain.community.comment.service.implementation;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.comment.api.v2.dto.response.ChildCommentResponseDto;
import net.causw.app.main.domain.community.comment.api.v2.mapper.ChildCommentResponseDtoMapper;
import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.repository.ChildCommentRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChildCommentReader {

	private final LikeChildCommentReader likeChildCommentReader;
	private final BoardConfigReader boardConfigReader;
	private final ChildCommentRepository childCommentRepository;
	private final ChildCommentResponseDtoMapper childCommentResponseDtoMapper;

	public ChildComment findById(String childCommentId) {
		return childCommentRepository.findById(childCommentId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.COMMENT_NOT_FOUND));
	}

	/**
	 * @param childComment 댓글
	 * @param user 조회자
	 * @param board 게시판
	 * @return CommentResponseDto
	 */
	public ChildCommentResponseDto getChildCommentDetail(ChildComment childComment, User user, Board board) {

		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(board.getId());

		long numLike = likeChildCommentReader.getNumOfChildCommentLikes(childComment);
		boolean isLiked = likeChildCommentReader.isChildCommentLiked(user, childComment.getId());

		boolean isOwner = childComment.getWriter().getId().equals(user.getId());
		boolean updatable = isOwner || boardAdminIds.contains(user.getId());
		boolean deletable = isOwner || boardAdminIds.contains(user.getId());

		return childCommentResponseDtoMapper.toChildCommentResponseDto(
			childComment,
			numLike,
			isLiked,
			isOwner,
			updatable,
			deletable,
			false);
	}

	/**
	 * @param childComment 대댓글
	 * @param user 조회자
	 * @param blockedUserIds 차단된 유저 ID 목록
	 * @param boardAdminIds 해당 게시판의 관리자 목록
	 * @param likeCount 대댓글 좋아요 수
	 * @param isLiked 현재 유저의 대댓글 좋아요 여부
	
	 * @return ChildCommentResponseDto
	 */
	public ChildCommentResponseDto getChildCommentListDetails(
		ChildComment childComment, User user, Set<String> blockedUserIds,
		List<String> boardAdminIds, long likeCount, boolean isLiked) {

		boolean isBlockedContent = blockedUserIds.contains(childComment.getWriter().getId());

		boolean isOwner = childComment.getWriter().getId().equals(user.getId());
		boolean updatable = isOwner || boardAdminIds.contains(user.getId());
		boolean deletable = isOwner || boardAdminIds.contains(user.getId());

		return childCommentResponseDtoMapper.toChildCommentResponseDto(
			childComment,
			likeCount,
			isLiked,
			isOwner,
			updatable,
			deletable,
			isBlockedContent);
	}

	public List<ChildComment> getChildCommentsByParentIds(List<String> parentCommentIds) {
		return childCommentRepository.findChildCommentsByParentCommentIds(parentCommentIds);
	}
}
