package net.causw.app.main.domain.community.board.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 게시판 도메인 검증 진입점.
 * 생성/수정 시 필요한 검증(이름 중복 등)을 한곳에서 수행한다.
 */
@Component
@RequiredArgsConstructor
public class BoardValidator {

	private final BoardNameDuplicateValidator boardNameDuplicateValidator;

	/**
	 * 게시판 생성 시 검증 (이름 중복 및 설정 검증).
	 */
	public void validateForCreate(String boardName, boolean isNotice, boolean isAnonymous) {
		boardNameDuplicateValidator.validateForCreate(boardName);
		validateConfig(isNotice, isAnonymous);
	}

	/**
	 * 게시판 수정 시 검증 (이름 중복 등, 해당 게시판 제외).
	 */
	public void validateForUpdate(String boardName, String excludeBoardId, boolean isNotice, boolean isAnonymous) {
		boardNameDuplicateValidator.validateForUpdate(boardName, excludeBoardId);
		validateConfig(isNotice, isAnonymous);
	}

	/**
	 * 게시판 설정(Config) 검증
	 */
	private void validateConfig(boolean isNotice, boolean isAnonymous) {
		if (isNotice && isAnonymous) {
			throw BoardErrorCode.BOARD_NOTICE_CANNOT_BE_ANONYMOUS.toBaseException();
		}
	}
}
