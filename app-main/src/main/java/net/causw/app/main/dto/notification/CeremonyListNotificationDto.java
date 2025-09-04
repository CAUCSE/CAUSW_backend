package net.causw.app.main.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CeremonyListNotificationDto {
	private String id;
	private String writer;      // 이름(학번)
	private String category;    // 경조사 종류
	private String date;        // 20xx.0x.0x ~ 20xx.0x.0x
	private String description; // 설명
	private String createdAt;   // 알림 신청일
}
