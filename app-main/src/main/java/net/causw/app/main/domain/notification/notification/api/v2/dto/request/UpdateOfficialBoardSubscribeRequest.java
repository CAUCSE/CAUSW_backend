package net.causw.app.main.domain.notification.notification.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공식계정 게시판 구독 수정 요청")
public record UpdateOfficialBoardSubscribeRequest(

	@NotNull @Schema(description = "구독 여부", requiredMode = Schema.RequiredMode.REQUIRED) Boolean subscribed

) {
}
