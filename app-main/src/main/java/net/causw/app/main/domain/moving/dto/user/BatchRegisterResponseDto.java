package net.causw.app.main.domain.moving.dto.user;

import java.util.List;

public record BatchRegisterResponseDto(Integer successCount, Integer failureCount, List<String> failureMessages) {
}
