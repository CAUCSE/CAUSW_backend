package net.causw.app.main.domain.community.board.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ENROLLED	재학생
 * GRADUATED	졸업생
 * BOTH	모두
 */
@Getter
@AllArgsConstructor
public enum BoardReadScope {
	ENROLLED("재학생"),
	GRADUATED("졸업생"),
	BOTH("모두");

	private final String description;
}
