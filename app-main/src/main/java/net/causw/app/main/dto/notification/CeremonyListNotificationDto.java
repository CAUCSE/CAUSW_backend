package net.causw.app.main.dto.notification;

import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.domain.model.entity.ceremony.Ceremony;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class CeremonyListNotificationDto {
    private String id;
    private String writer;      // 이름(학번)
    private String category;    // 경조사 종류
    private String date;        // 20xx.0x.0x ~ 20xx.0x.0x
    private String description; // 설명
    private String createdAt;   // 알림 신청일

    public static CeremonyListNotificationDto of(Ceremony ceremony) {
        return CeremonyListNotificationDto.builder()
                .id(ceremony.getId())
                .writer(String.format("%s(%s)",
                        ceremony.getUser().getName(),
                        ceremony.getUser().getAdmissionYear() % 100))
                .category(ceremony.getCeremonyCategory().getLabel())
                .date(String.format("%s ~ %s",
                        ceremony.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        ceremony.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))))
                .description(ceremony.getDescription())
                .createdAt(ceremony.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .build();
    }
}
