package net.causw.domain.model;

import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

@Getter
public enum UserState {
    AWAIT("await"),
    ACTIVE("active"),
    INACTIVE("inactive"),
    REJECT("reject"),
    DROP("drop");

    private String value;

    UserState(String value) {
        this.value = value;
    }

    public static UserState of(String value) {
        return Arrays.stream(values())
                .filter(v -> value.equalsIgnoreCase(v.value))
                .findFirst()
                .orElseThrow(
                        () -> new BadRequestException(
                                ErrorCode.INVALID_REQUEST_USER_STATE,
                                String.format("'%s' is invalid : not supported", value)
                        )
                );
    }
}
