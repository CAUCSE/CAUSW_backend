package net.causw.app.main.domain.community.ceremony.api.v2.dto.response;

import java.util.Set;

import net.causw.app.main.domain.notification.notification.entity.CeremonyNotificationSetting;

public record CeremonyNotificationSettingResponseDto(
	boolean isNotificationActive,
	boolean isSetAll,
	Set<String> subscribedAdmissionYears) {

	public static CeremonyNotificationSettingResponseDto from(
		CeremonyNotificationSetting ceremonyNotificationSetting) {
		return new CeremonyNotificationSettingResponseDto(
			ceremonyNotificationSetting.isNotificationActive(),
			ceremonyNotificationSetting.isSetAll(),
			ceremonyNotificationSetting.getSubscribedAdmissionYears());
	}
}