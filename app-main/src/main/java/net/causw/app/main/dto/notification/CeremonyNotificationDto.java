package net.causw.app.main.dto.notification;

import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.domain.model.entity.ceremony.Ceremony;

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
                        ceremony.getCeremonyCategory().getLabel()))
                .body(String.format("기간 : %s ~ %s",
                        ceremony.getStartDate().toString(),
                        ceremony.getEndDate().toString()))
                .build();
    }
}
