package net.causw.app.main.domain.community.board.util;

import org.springframework.stereotype.Component;

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
	 * 게시판 생성 시 검증 (이름 중복 등).
	 */
	public void validateForCreate(String boardName) {
		boardNameDuplicateValidator.validateForCreate(boardName);
	}

	/**
	 * 게시판 수정 시 검증 (이름 중복 등, 해당 게시판 제외).
	 */
	public void validateForUpdate(String boardName, String excludeBoardId) {
		boardNameDuplicateValidator.validateForUpdate(boardName, excludeBoardId);
	}
}
