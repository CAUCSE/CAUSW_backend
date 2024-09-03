package net.causw.domain.model.enums;

import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

@Getter
public enum UserState {
    AWAIT("AWAIT"),
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    REJECT("REJECT"),
    DROP("DROP"),
    DELETED("DELETED");

    private final String value;

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
