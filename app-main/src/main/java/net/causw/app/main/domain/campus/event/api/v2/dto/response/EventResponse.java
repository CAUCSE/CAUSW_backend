package net.causw.app.main.domain.campus.event.api.v2.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record EventResponse(
	@Schema(description = "이벤트 ID") String id,
	@Schema(description = "이벤트 제목") String title,
	@Schema(description = "배너 이미지 URL") String imageUrl,
	@Schema(description = "배너 클릭 시 이동할 링크 (없으면 null)") String landingUrl,
	@Schema(description = "홈 화면 노출 여부") Boolean isActive,
	@Schema(description = "노출 순서 (오름차순)") Integer displayOrder,
	@Schema(description = "생성 시각") LocalDateTime createdAt,
	@Schema(description = "수정 시각") LocalDateTime updatedAt) {
}
