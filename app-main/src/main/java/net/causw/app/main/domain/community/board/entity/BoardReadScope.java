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
	 * 이 읽기 범위에 해당하는 학적 상태 목록 반환.
	 * <p>빈 리스트는 모든 학적 상태를 허용함({@code BOTH})을 의미합니다.
	 *
	 * @return 해당 읽기 범위에 접근 가능한 학적 상태 목록
	 */
	public List<AcademicStatus> getTargetAcademicStatuses() {
		return switch (this) {
			case ENROLLED -> List.of(
				AcademicStatus.ENROLLED,
				AcademicStatus.LEAVE_OF_ABSENCE,
				AcademicStatus.SUSPEND,
				AcademicStatus.PROFESSOR
			);
			case GRADUATED -> List.of(AcademicStatus.GRADUATED);
			case BOTH -> List.of(); // 빈 리스트 = 모든 학적 허용
		};
	}

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
