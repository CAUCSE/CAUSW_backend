package net.causw.domain.model;

import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

@Getter
public enum Role {
    ADMIN("admin"),
    PRESIDENT("president"),
    COUNCIL("council"),
    LEADER_1("leader_1"),
    LEADER_2("leader_2"),
    LEADER_3("leader_3"),
    LEADER_4("leader_4"),
    LEADER_CIRCLE("leader_circle"),
    LEADER_ALUMNI("leader_alumni"),
    COMMON("common"),
    NONE("none"),
    PROFESSOR("professor");

    private String value;

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
