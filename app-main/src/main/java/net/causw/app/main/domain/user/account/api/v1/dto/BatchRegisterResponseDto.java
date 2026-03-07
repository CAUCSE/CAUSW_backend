package net.causw.app.main.domain.user.account.api.v1.dto;

import java.util.List;

public record BatchRegisterResponseDto(Integer successCount, Integer failureCount, List<String> failureMessages) {
}
