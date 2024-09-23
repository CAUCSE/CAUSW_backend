package net.causw.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegisteredSemester {

    FIRST_SEMESTER("1차 학기"),
    SECOND_SEMESTER("2차 학기"),
    THIRD_SEMESTER("3차 학기"),
    FOURTH_SEMESTER("4차 학기"),
    FIFTH_SEMESTER("5차 학기"),
    SIXTH_SEMESTER("6차 학기"),
    SEVENTH_SEMESTER("7차 학기"),
    EIGHTH_SEMESTER("8차 학기");

    private final String value;

}
