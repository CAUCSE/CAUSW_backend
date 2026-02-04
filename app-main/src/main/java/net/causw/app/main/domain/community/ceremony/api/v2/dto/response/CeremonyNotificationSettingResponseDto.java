package net.causw.app.main.domain.community.ceremony.api.v2.dto.response;

import java.util.Set;

import net.causw.app.main.domain.notification.notification.entity.CeremonyNotificationSetting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CeremonyNotificationSettingResponseDto {
	private boolean isNotificationActive;
	private boolean isSetAll;
	private Set<String> subscribedAdmissionYears;

	public static CeremonyNotificationSettingResponseDto from(CeremonyNotificationSetting ceremonyNotificationSetting) {
		return CeremonyNotificationSettingResponseDto.builder()
			.isNotificationActive(ceremonyNotificationSetting.isNotificationActive())
			.subscribedAdmissionYears(ceremonyNotificationSetting.getSubscribedAdmissionYears())
			.isSetAll(ceremonyNotificationSetting.isSetAll())
			.build();
	}
}
