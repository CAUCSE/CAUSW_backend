package net.causw.domain.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum UserState {
    AWAIT("AWAIT", "가입 대기"),
    ACTIVE("ACTIVE", "활성"),
    INACTIVE("INACTIVE", "탈퇴"),
    REJECT("REJECT", "가입 거부"),
    DROP("DROP", "추방"),
    DELETED("DELETED", "삭제됨");

    private final String value;
    private final String description;

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
