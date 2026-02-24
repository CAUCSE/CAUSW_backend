package net.causw.app.main.domain.community.board.entity;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ENROLLED	재학생
 * GRADUATED	졸업생
 * BOTH	모두
 * 유저별 게시판 조회 범위를 나타내는 Enum
 * 조회 로직은 아래 클래스의 메서드에서 담당
 * @see net.causw.app.main.domain.community.board.service.implementation.BoardReader#getReadeScopesByAcademicStatus(AcademicStatus)
 */
@Getter
@AllArgsConstructor
public enum BoardReadScope {
	ENROLLED("재학생"),
	GRADUATED("졸업생"),
	BOTH("모두");

	private final String description;
}
