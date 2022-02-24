package net.causw.domain.model;

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
                .orElse(null);
    }
}
