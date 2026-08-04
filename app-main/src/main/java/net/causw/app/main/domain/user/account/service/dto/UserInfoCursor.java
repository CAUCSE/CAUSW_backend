package net.causw.app.main.domain.user.account.service.dto;

import java.time.Instant;

public record UserInfoCursor(
        String section,
        Instant createdAt,
        String userInfoId
) {
}
