package net.causw.domain.model;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

public enum SearchOption {
    TITLE("TITLE"),
    WRITER("WRITER");

    private final String value;

    SearchOption(String value) {
        this.value = value;
    }

    public static SearchOption of(String value) {
        return Arrays
                .stream(values())
                .filter(v -> value.equalsIgnoreCase(v.value))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.INVALID_PARAMETER,
                        "잘못된 검색 옵션입니다."
                ));
    }
}
