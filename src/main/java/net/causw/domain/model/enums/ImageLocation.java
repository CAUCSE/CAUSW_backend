package net.causw.domain.model.enums;

import java.util.Arrays;

public enum ImageLocation {
    USER_PROFILE("USER_PROFILE"),
    USER_ADMISSION("USER_ADMISSION"),
    CIRCLE_PROFILE("CIRCLE_PROFILE"),
    POST("POST"),
    ETC("ETC");

    private final String value;

    ImageLocation(String value) {
        this.value = value;
    }

    public static ImageLocation of(String value) {
        return Arrays
                .stream(values())
                .filter(v -> value.equalsIgnoreCase(v.value))
                .findFirst()
                .orElse(null);
    }
}
