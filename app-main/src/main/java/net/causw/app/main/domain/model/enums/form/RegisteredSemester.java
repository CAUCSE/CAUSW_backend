package net.causw.app.main.domain.model.enums.form;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegisteredSemester {

	FIRST_SEMESTER("1차 학기", 1),
	SECOND_SEMESTER("2차 학기", 2),
	THIRD_SEMESTER("3차 학기", 3),
	FOURTH_SEMESTER("4차 학기", 4),
	FIFTH_SEMESTER("5차 학기", 5),
	SIXTH_SEMESTER("6차 학기", 6),
	SEVENTH_SEMESTER("7차 학기", 7),
	EIGHTH_SEMESTER("8차 학기", 8),
	ABOVE_NINTH_SEMESTER("9차 학기 이상", 9);

	private final String value;
	private final Integer semester;

}
