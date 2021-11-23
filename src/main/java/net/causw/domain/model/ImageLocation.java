package net.causw.domain.model;

import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

public enum ImageLocation {
    USER_PROFILE("user_profile"),
    USER_ADMISSION("user_admission"),
    CIRCLE_PROFILE("circle_profile"),
    POST("post"),
    ETC("etc");

    private String value;

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
