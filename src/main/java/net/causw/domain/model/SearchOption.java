package net.causw.domain.model;

import java.util.Arrays;

public enum SearchOption {
    TITLE("title"),
    WRITER("writer");

    private String value;

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
