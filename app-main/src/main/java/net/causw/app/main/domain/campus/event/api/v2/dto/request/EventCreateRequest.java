package net.causw.app.main.domain.campus.event.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record EventCreateRequest(
	@Schema(description = "이벤트 제목", example = "ICT 위원회 모집") @NotBlank @Size(max = 255) String title,

	@Schema(description = "Presigned URL(FilePath.EVENT)로 업로드한 배너 이미지 UUID") @NotBlank String imageUuid,

	@Schema(description = "배너 클릭 시 이동할 링크 (선택)", example = "https://causw.net") @Pattern(regexp = "^https?://.+", message = "랜딩 링크는 http:// 또는 https:// 로 시작해야 합니다.") String landingUrl,

	@Schema(description = "홈 화면 노출 여부", example = "true") @NotNull Boolean isActive,

	@Schema(description = "노출 순서 (오름차순)", example = "0") @NotNull @PositiveOrZero Integer displayOrder) {
}
