package net.causw.domain.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    /**
     * 400 Bad Request
     */
    ROW_DOES_NOT_EXIST(4000),
    ROW_ALREADY_EXIST(4001),
    INVALID_PARAMETER(4002),            // Parameter format error
    INVALID_USER_DATA_REQUEST(4003),    // User signup & update validation error
    TARGET_DELETED(4004),
    INVALID_HTTP_METHOD(4005),
    APPLY_NOT_EXIST(4006),
    CANNOT_PERFORMED(4007),
    AWAITING_STATUS(4008),
    INVALID_STUDENT_ID(4009),

    /**
     * 401 Unauthorized
     */
    API_NOT_ACCESSIBLE(4100),
    INVALID_SIGNIN(4101),
    BLOCKED_USER(4102),
    INACTIVE_USER(4103),
    AWAITING_USER(4104),
    INVALID_JWT(4105),
    GRANT_ROLE_NOT_ALLOWED(4106),
    API_NOT_ALLOWED(4107),

    /**
     * 500 Internal Server Error
     */
    INTERNAL_SERVER(5000);

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }
}
