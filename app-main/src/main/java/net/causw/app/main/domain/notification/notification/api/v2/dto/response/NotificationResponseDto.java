package net.causw.app.main.domain.notification.notification.api.v2.dto.response;

import net.causw.app.main.domain.notification.notification.enums.NoticeType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NotificationResponseDto(
	@Schema(description = "알람 로그 id", example = "uuid 형식을 String 값입니다.") String notificationLogId,

	@Schema(description = "알림 제목", example = "알림 제목입니다.") String title,

	@Schema(description = "알림 내용", example = "알림 내용입니다.") String body,

	@Schema(description = "알림 종류", example = "CEREMONY") NoticeType noticeType,

	@Schema(description = "조회할 게시글 id(경조사 or 게시글)", example = "uuid 형식의 String 값입니다") String targetId,

	@Schema(description = "조회할 타겟의 부모 id(게시글의 경우 게시판)", example = "uuid 형식의 String 값입니다") String targetParentId,

	@Schema(description = "알람 확인 여부", example = "true/false") Boolean isRead) {
}
