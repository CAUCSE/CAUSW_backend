package net.causw.app.main.domain.community.ceremony.api.v1.dto;

import java.util.Set;
import java.util.stream.Collectors;

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
		Set<String> years2digit = ceremonyNotificationSetting.getSubscribedAdmissionYears().stream()
			.map(year -> String.format("%02d", year % 100))
			.collect(Collectors.toSet());
		return CeremonyNotificationSettingResponseDto.builder()
			.isNotificationActive(ceremonyNotificationSetting.isNotificationActive())
			.subscribedAdmissionYears(years2digit)
			.isSetAll(ceremonyNotificationSetting.isSetAll())
			.build();
	}
}
