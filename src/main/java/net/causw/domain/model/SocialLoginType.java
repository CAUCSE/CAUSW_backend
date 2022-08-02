package net.causw.domain.model;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

public enum SocialLoginType {
    KAKAO("KAKAO"),
    NAVER("NAVER");

    private final String value;

    SocialLoginType(String value) {
        this.value = value;
    }

    public static SocialLoginType of(String value) {
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
