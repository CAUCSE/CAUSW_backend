package net.causw.application.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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

    @Schema(description = "조회할 게시글 id(경조사 or 게시글)", example = "uuid 형식의 String 값입니다")
    private String targetId;

    @Schema(description = "알람 확인 여부", example = "true/false")
    private Boolean isRead;

}
