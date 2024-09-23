package net.causw.domain.model.enums.locker;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

public enum LockerLogAction {
    ENABLE("ENABLE"),
    DISABLE("DISABLE"),
    REGISTER("REGISTER"),
    RETURN("RETURN"),
    EXTEND("EXTEND");

    private final String value;

    LockerLogAction(String value) {
        this.value = value;
    }

    public static LockerLogAction of(String value) {
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
