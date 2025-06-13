package net.causw.domain.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {
    ADMIN("ADMIN", "관리자", false),
    PRESIDENT("PRESIDENT", "학생회장", true),
    VICE_PRESIDENT("VICE_PRESIDENT", "부학생회장", true),
    COUNCIL("COUNCIL", "학생회", false),
    LEADER_1("LEADER_1", "1학년 대표", false),
    LEADER_2("LEADER_2", "2학년 대표", false),
    LEADER_3("LEADER_3", "3학년 대표", false),
    LEADER_4("LEADER_4", "4학년 대표", false),
    LEADER_ALUMNI("LEADER_ALUMNI", "동문회장", true),
    COMMON("COMMON", "일반", false),
    NONE("NONE", "없음", false),

    // Deprecated Roles
    @Deprecated LEADER_CIRCLE("LEADER_CIRCLE", "동아리장", false),
    @Deprecated PROFESSOR("PROFESSOR", "교수", false);

    private final String value;
    private final String description;
    private final boolean unique;

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
