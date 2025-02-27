package net.causw.application.dto.notification;

import lombok.Builder;
import lombok.Getter;
import net.causw.adapter.persistence.ceremony.Ceremony;

@Getter
@Builder
public class CeremonyNotificationDto {
    private String title;
    private String body;

    public static CeremonyNotificationDto of(Ceremony ceremony) {
        return CeremonyNotificationDto.builder()
                .title(String.format("%s %s - %s",
                        ceremony.getUser().getAdmissionYear().toString(),
                        ceremony.getUser().getName(),
                        ceremony.getCeremonyCategory().toString()))
                .body(String.format("Description: %s\nStart Date: %s\nEnd Date: %s",
                        ceremony.getDescription(),
                        ceremony.getStartDate().toString(),
                        ceremony.getEndDate().toString()))
                .build();
    }
}
