package net.causw.app.main.service.userBlock.useCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.userBlock.response.CreateBlockByCommentResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.comment.CommentEntityService;
import net.causw.app.main.service.userBlock.UserBlockEntityService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockByCommentUseCaseService {

	private final CommentEntityService commentEntityService;
	private final UserBlockEntityService userBlockService;

	public CreateBlockByCommentResponseDto execute(CustomUserDetails userDetails, String commentId) {

		User currentUser = userDetails.getUser();
		Comment comment = commentEntityService.findByIdNotDeleted(commentId);
		User commentWriter = comment.getWriter();

		boolean isAlreadyBlocked = userBlockService.existsBlockByUsers(currentUser, commentWriter);

		if (comment.getIsAnonymous()) {
			return handleAnonymousCommentBlock(currentUser, comment, isAlreadyBlocked);
		}

		return handleRegularCommentBlock(currentUser, comment, isAlreadyBlocked);
	}

	/**
	 * 익명인 경우 동일인을 다시 차단하더라도 익명성 유지를 위해 차단에 성공했다는 응답 제공
	 *
	 * @param currentUser 차단 시도자
	 * @param comment 차단 대상 댓글
	 * @param isAlreadyBlocked 기존 차단 존재 여부
	 * @return 차단 성공 응답
	 */
	private CreateBlockByCommentResponseDto handleAnonymousCommentBlock(User currentUser, Comment comment,
		boolean isAlreadyBlocked) {
		if (isAlreadyBlocked) {
			return new CreateBlockByCommentResponseDto(MessageUtil.BLOCK_SUCCESS);
		}

		userBlockService.createBlock(currentUser, comment);

		return new CreateBlockByCommentResponseDto(MessageUtil.BLOCK_SUCCESS);
	}

	/**
	 * 기명 유저를 차단하는 경우 다시 차단하는 경우 예외 응답 제공
	 *
	 * @param currentUser 차단 시도자
	 * @param comment 차단 대상 댓글
	 * @param isAlreadyBlocked 기존 차단 존재 여부
	 * @return 차단 성공 응답
	 */
	private CreateBlockByCommentResponseDto handleRegularCommentBlock(User currentUser, Comment comment,
		boolean isAlreadyBlocked) {
		if (isAlreadyBlocked) {
			throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.BLOCK_ALREADY_EXIST);
		}

		userBlockService.createBlock(currentUser, comment);

		return new CreateBlockByCommentResponseDto(MessageUtil.BLOCK_SUCCESS);
	}

}
