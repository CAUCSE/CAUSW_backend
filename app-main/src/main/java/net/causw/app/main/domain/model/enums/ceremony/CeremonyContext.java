package net.causw.app.main.domain.model.enums.ceremony;

import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

public enum CeremonyContext {
    GENERAL("general", "전체 알림 페이지에서 접근"),
    MY("my", "나의 경조사 페이지에서 접근"),
    ADMIN("admin", "경조사 관리 페이지에서 접근");

    private final String value;
    private final String description;

    CeremonyContext(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() { return value; }
    public String getDescription() { return description; }

    // String을 enum으로 변환
    public static CeremonyContext fromString(String context) {
        if (context == null || context.isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "context 값은 필수입니다."
            );
        }

        for (CeremonyContext ceremonyContext : CeremonyContext.values()) {
            if (ceremonyContext.value.equalsIgnoreCase(context)) {
                return ceremonyContext;
            }
        }

        throw new BadRequestException(
                ErrorCode.INVALID_PARAMETER,
                "유효하지 않은 context 값입니다. 사용 가능한 값: general, my, admin"
        );
    }
}
