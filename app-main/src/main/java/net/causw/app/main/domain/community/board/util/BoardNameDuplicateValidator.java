package net.causw.app.main.domain.community.board.util;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 게시판 이름 중복 검사 validator.
 * Create 시에는 이름 존재 여부만, Update 시에는 해당 게시판을 제외한 이름 존재 여부를 검사한다.
 */
@Component
@RequiredArgsConstructor
public class BoardNameDuplicateValidator {

	private final BoardReader boardReader;

	/**
	 * 생성 시 게시판 이름 중복 검사.
	 * 이미 존재하는 이름이면 {@link BoardErrorCode#BOARD_NAME_DUPLICATE} 예외를 던진다.
	 */
	public void validateForCreate(String name) {
		if (boardReader.existsByName(name)) {
			throw BoardErrorCode.BOARD_NAME_DUPLICATE.toBaseException();
		}
	}

	/**
	 * 수정 시 게시판 이름 중복 검사.
	 * 해당 게시판(excludeBoardId)을 제외한 다른 게시판이 동일 이름을 쓰면 {@link BoardErrorCode#BOARD_NAME_DUPLICATE} 예외를 던진다.
	 */
	public void validateForUpdate(String name, String excludeBoardId) {
		if (boardReader.existsByNameExcludingId(name, excludeBoardId)) {
			throw BoardErrorCode.BOARD_NAME_DUPLICATE.toBaseException();
		}
	}
}
