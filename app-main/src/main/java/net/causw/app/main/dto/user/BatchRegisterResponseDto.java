package net.causw.app.main.dto.user;

import java.util.List;

public record BatchRegisterResponseDto(Integer successCount, Integer failureCount, List<String> failureMessages) {
}
