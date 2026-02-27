package net.causw.app.main.domain.community.board.entity;

import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ENROLLED	재학생
 * GRADUATED	졸업생
 * BOTH	모두
 * 유저별 게시판 조회 범위를 나타내는 Enum
 */
@Getter
@AllArgsConstructor
public enum BoardReadScope {
	ENROLLED("재학생"),
	GRADUATED("졸업생"),
	BOTH("모두");

	private final String description;

	/**
	 * 사용자의 학적 상태에 따른 읽기 범위 리스트 반환
	 *
	 * @param academicStatus 사용자의 학적 상태
	 * @return 사용자의 학적 상태에 따른 읽기 범위 리스트
	 */
	public static List<BoardReadScope> fromAcademicStatus(AcademicStatus academicStatus) {
		if (academicStatus == null) {
			return List.of(BOTH);
		}
		return switch (academicStatus) {
			case ENROLLED, LEAVE_OF_ABSENCE, SUSPEND, PROFESSOR -> List.of(BOTH, ENROLLED);
			case GRADUATED -> List.of(BOTH, GRADUATED);
			default -> List.of(BOTH);
		};
	}
}
