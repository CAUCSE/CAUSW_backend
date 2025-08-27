package net.causw.app.main.service.userBlock.useCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.userBlock.response.CreateBlockByChildCommentResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.comment.ChildCommentEntityService;
import net.causw.app.main.service.userBlock.UserBlockEntityService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockByChildCommentUseCaseService {

	private final ChildCommentEntityService childCommentEntityService;
	private final UserBlockEntityService userBlockService;

	public CreateBlockByChildCommentResponseDto execute(CustomUserDetails userDetails, String commentId) {

		User currentUser = userDetails.getUser();
		ChildComment childComment = childCommentEntityService.findByIdNotDeleted(commentId);
		User commentWriter = childComment.getWriter();

		validateSelfBlock(currentUser, commentWriter);

		boolean isAlreadyBlocked = userBlockService.existsBlockByUsers(currentUser, commentWriter);

		if (childComment.getIsAnonymous()) {
			return handleAnonymousCommentBlock(currentUser, childComment, isAlreadyBlocked);
		}

		return handleRegularCommentBlock(currentUser, childComment, isAlreadyBlocked);
	}

	/**
	 * 차단 시도자와 대상자가 같은 인물인지 확인하는 로직
	 *
	 * @param currentUser 차단 시도자
	 * @param postWriter 차단 대상자
	 */
	private static void validateSelfBlock(User currentUser, User postWriter) {
		if (currentUser.equals(postWriter)) {
			throw new BadRequestException(ErrorCode.CANNOT_PERFORMED, MessageUtil.CANNOT_BLOCK_SELF);
		}
	}

	/**
	 * 익명인 경우 동일인을 다시 차단하더라도 익명성 유지를 위해 차단에 성공했다는 응답 제공
	 *
	 * @param currentUser      차단 시도자
	 * @param childComment     차단 대상 대댓글
	 * @param isAlreadyBlocked 기존 차단 존재 여부
	 * @return 차단 성공 응답
	 */
	private CreateBlockByChildCommentResponseDto handleAnonymousCommentBlock(User currentUser, ChildComment childComment, boolean isAlreadyBlocked) {
		if (isAlreadyBlocked) {
			return new CreateBlockByChildCommentResponseDto(MessageUtil.BLOCK_SUCCESS);
		}

		userBlockService.createBlock(currentUser, childComment);

		return new CreateBlockByChildCommentResponseDto(MessageUtil.BLOCK_SUCCESS);
	}

	/**
	 * 기명 유저를 차단하는 경우 다시 차단하는 경우 예외 응답 제공
	 *
	 * @param currentUser      차단 시도자
	 * @param childComment     차단 대상 대댓글
	 * @param isAlreadyBlocked 기존 차단 존재 여부
	 * @return 차단 성공 응답
	 */
	private CreateBlockByChildCommentResponseDto handleRegularCommentBlock(User currentUser, ChildComment childComment, boolean isAlreadyBlocked) {
		if (isAlreadyBlocked) {
			throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.BLOCK_ALREADY_EXIST);
		}

		userBlockService.createBlock(currentUser, childComment);

		return new CreateBlockByChildCommentResponseDto(MessageUtil.BLOCK_SUCCESS);
	}

}
