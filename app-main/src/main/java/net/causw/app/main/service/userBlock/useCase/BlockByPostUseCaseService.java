package net.causw.app.main.service.userBlock.useCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.dto.userBlock.response.CreateBlockByPostResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.post.PostEntityService;
import net.causw.app.main.service.userBlock.UserBlockEntityService;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockByPostUseCaseService {

	private final PostEntityService postService;
	private final UserBlockEntityService userBlockService;

	public CreateBlockByPostResponseDto execute(CustomUserDetails userDetails, String postId) {

		User currentUser = userDetails.getUser();
		Post post = postService.findByIdNotDeleted(postId);
		User postWriter = post.getWriter();

		validateSelfBlock(currentUser, postWriter);

		boolean isAlreadyBlocked = userBlockService.existsBlockByUsers(currentUser, postWriter);

		if (post.getIsAnonymous()) {
			return handleAnonymousPostBlock(currentUser, post, isAlreadyBlocked);
		}

		return handleRegularPostBlock(currentUser, post, isAlreadyBlocked);
	}

	/**
	 * 차단 시도자와 대상자가 같은 인물인지 확인하는 로직
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
	 * @param currentUser 차단 시도자
	 * @param post 차단 대상 게시물
	 * @param isAlreadyBlocked 기존 차단 존재 여부
	 * @return 차단 성공 응답
	 */
	private CreateBlockByPostResponseDto handleAnonymousPostBlock(User currentUser, Post post, boolean isAlreadyBlocked) {
		if (isAlreadyBlocked) {
			return new CreateBlockByPostResponseDto(MessageUtil.BLOCK_SUCCESS);
		}

		userBlockService.createBlock(currentUser, post);
		return new CreateBlockByPostResponseDto(MessageUtil.BLOCK_SUCCESS);
	}

	/**
	 * 기명 유저를 차단하는 경우 다시 차단하는 경우 예외 응답 제공
	 * @param currentUser 차단 시도자
	 * @param post 차단 대상 게시물
	 * @param isAlreadyBlocked 기존 차단 존재 여부
	 * @return 차단 성공 응답
	 */
	private CreateBlockByPostResponseDto handleRegularPostBlock(User currentUser, Post post, boolean isAlreadyBlocked) {
		if (isAlreadyBlocked) {
			throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.BLOCK_ALREADY_EXIST);
		}

		userBlockService.createBlock(currentUser, post);
		return new CreateBlockByPostResponseDto(MessageUtil.BLOCK_SUCCESS);
	}

}
