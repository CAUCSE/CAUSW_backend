package net.causw.app.main.domain.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {
    ADMIN("ADMIN", "관리자"),
    PRESIDENT("PRESIDENT", "학생회장"),
    VICE_PRESIDENT("VICE_PRESIDENT", "부학생회장"),
    COUNCIL("COUNCIL", "학생회"),
    LEADER_1("LEADER_1", "1학년 대표"),
    LEADER_2("LEADER_2", "2학년 대표"),
    LEADER_3("LEADER_3", "3학년 대표"),
    LEADER_4("LEADER_4", "4학년 대표"),
    LEADER_ALUMNI("LEADER_ALUMNI", "동문회장"),
    COMMON("COMMON", "일반"),
    NONE("NONE", "없음"),

    // Deprecated Roles
    @Deprecated LEADER_CIRCLE("LEADER_CIRCLE", "동아리장"),
    @Deprecated PROFESSOR("PROFESSOR", "교수");

    private final String value;
    private final String description;

    public static Role of(String value) {
        return Arrays.stream(values())
                .filter(v -> value.equalsIgnoreCase(v.value))
                .findFirst()
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.INVALID_REQUEST_ROLE,
                                String.format("'%s' is invalid : not supported", value)
                        )
                );
    }
}
