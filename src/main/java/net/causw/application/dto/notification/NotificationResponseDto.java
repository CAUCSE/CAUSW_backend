package net.causw.application.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.causw.adapter.persistence.notification.Notification;
import net.causw.domain.model.enums.notification.NoticeType;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NotificationResponseDto {

    @Schema(description = "알림 제목", example = "알림 제목입니다.")
    private String title;

    @Schema(description = "알림 내용", example = "알림 내용입니다.")
    private String body;

    @Schema(description = "알림 종류", example = "CEREMONY")
    private NoticeType noticeType;

    public static NotificationResponseDto of(Notification notification) {
        return NotificationResponseDto.builder()
//                .user_id(notification.getUser().getId().toString())
                .title(notification.getTitle())
                .body(notification.getBody())
                .noticeType(notification.getNoticeType())
//                .isGlobal(notification.getIsGlobal())
                .build();
    }
}
