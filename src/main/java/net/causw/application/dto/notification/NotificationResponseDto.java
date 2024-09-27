package net.causw.application.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.enums.NoticeType;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationResponseDto {
    @Schema(description = "사용자 id", example = "uuid 형식의 String 값입니다.")
    private String user_id;

    @Schema(description = "알림 내용", example = "알림 내용입니다.")
    private String content;

    @Schema(description = "알림 종류", example = "POST")
    private NoticeType noticeType;

    @Schema(description = "전체 사용자 대상 알리 여부", example = "false")
    private Boolean isGlobal;
}
