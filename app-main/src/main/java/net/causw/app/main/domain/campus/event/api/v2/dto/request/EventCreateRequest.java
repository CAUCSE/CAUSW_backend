package net.causw.app.main.domain.campus.event.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EventCreateRequest(
	@Schema(description = "이벤트 제목", example = "2학기 ICT 위원회 백엔드 신청") @NotBlank @Size(max = 255) String title,

	@Schema(description = "이벤트 클릭 시 이동할 링크", example = "https://causw.net") @NotBlank @Pattern(regexp = "^https?://.+", message = "랜딩 링크는 http:// 또는 https:// 로 시작해야 합니다.") String landingUrl,

	@Schema(description = "홈 화면 노출 여부", example = "true") @NotNull Boolean isActive) {
}
