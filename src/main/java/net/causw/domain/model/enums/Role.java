package net.causw.domain.model.enums;

import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

@Getter
public enum Role {
    ADMIN("ADMIN"),
    PRESIDENT("PRESIDENT"),
    PRESIDENT_N_LEADER_CIRCLE("PRESIDENT_N_LEADER_CIRCLE"),

    VICE_PRESIDENT("VICE_PRESIDENT"),
    VICE_PRESIDENT_N_LEADER_CIRCLE("VICE_PRESIDENT_N_LEADER_CIRCLE"),

    COUNCIL("COUNCIL"),
    LEADER_1("LEADER_1"),
    LEADER_2("LEADER_2"),
    LEADER_3("LEADER_3"),
    LEADER_4("LEADER_4"),
    LEADER_CIRCLE("LEADER_CIRCLE"),
    LEADER_ALUMNI("LEADER_ALUMNI"),
    COMMON("COMMON"),
    NONE("NONE"),
    PROFESSOR("PROFESSOR"),

    COUNCIL_N_LEADER_CIRCLE("COUNCIL_N_LEADER_CIRCLE"),
    LEADER_1_N_LEADER_CIRCLE("LEADER_1_N_LEADER_CIRCLE"),
    LEADER_2_N_LEADER_CIRCLE("LEADER_2_N_LEADER_CIRCLE"),
    LEADER_3_N_LEADER_CIRCLE("LEADER_3_N_LEADER_CIRCLE"),
    LEADER_4_N_LEADER_CIRCLE("LEADER_4_N_LEADER_CIRCLE");

    private final String value;

    Role(String value) {
        this.value = value;
    }

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
