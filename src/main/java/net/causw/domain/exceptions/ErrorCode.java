package net.causw.domain.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ROW_DOES_NOT_EXIST(4000),
    ROW_ALREADY_EXIST(4001),
    INVALID_PARAMETER(4002),

    INVALID_SIGNIN(4100),
    INVALID_SIGNUP(4101),
    INVALID_UPDATE_USER(4102),
    BLOCKED_USER(4103),
    INACTIVE_USER(4104),
    EXPIRED_JWT(4105),
    API_NOT_ACCESSIBLE(4106),

    INTERNAL_SERVER(5000);

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }
}
