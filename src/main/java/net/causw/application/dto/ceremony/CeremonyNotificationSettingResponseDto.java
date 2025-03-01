package net.causw.application.dto.ceremony;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.notification.CeremonyNotificationSetting;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class CeremonyNotificationSettingResponseDto {
    private boolean isNotificationActive;
    private boolean isSetAll;
    private Set<Integer> subscribedAdmissionYears;


    public static CeremonyNotificationSettingResponseDto from(CeremonyNotificationSetting ceremonyNotificationSetting) {
        return CeremonyNotificationSettingResponseDto.builder()
                .isNotificationActive(ceremonyNotificationSetting.isNotificationActive())
                .subscribedAdmissionYears(ceremonyNotificationSetting.getSubscribedAdmissionYears())
                .isSetAll(ceremonyNotificationSetting.isSetAll())
                .build();
    }
}
