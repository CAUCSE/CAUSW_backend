package net.causw.domain.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GraduationType {
    FEBRUARY("2월 졸업"), // 2월 졸업
    AUGUST("8월 졸업"); // 8월 졸업

    private final String value;
}
