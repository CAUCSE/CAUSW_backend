package net.causw.app.main.domain.notification.notification.api.v1.dto;

import net.causw.app.main.domain.community.ceremony.entity.Ceremony;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CeremonyNotificationDto {
	private String id;
	private String title;
	private String body;

	public static CeremonyNotificationDto of(Ceremony ceremony) {
		return CeremonyNotificationDto.builder()
			.id(ceremony.getId())
			.title(String.format("%s(%s) - %s",
				ceremony.getUser().getName(),
				ceremony.getUser().getAdmissionYear().toString(),
				ceremony.getCeremonyCategory()))
			.body(String.format("기간 : %s ~ %s",
				ceremony.getStartDate().toString(),
				ceremony.getEndDate().toString()))
			.build();
	}
}
