package net.causw.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SemesterType {
    FIRST("1학기"),
    SECOND("2학기"),
    SUMMER("여름학기"),
    WINTER("겨울학기");

    private final String value;
}
