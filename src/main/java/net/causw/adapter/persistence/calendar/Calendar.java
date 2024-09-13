package net.causw.adapter.persistence.calendar;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_calendar")
public class Calendar extends BaseEntity {
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "calendar_attach_image_uuid_file_id", nullable = false)
    private UuidFile calendarAttachImageUuidFile;

    public static Calendar of(
            Integer year,
            Integer month,
            UuidFile calendarAttachImageUuidFile
    ) {
        return Calendar.builder()
                .year(year)
                .month(month)
                .calendarAttachImageUuidFile(calendarAttachImageUuidFile)
                .build();
    }

    public void update(Integer year, Integer month, UuidFile calendarAttachImageUuidFile) {
        this.year = year;
        this.month = month;
        this.calendarAttachImageUuidFile = calendarAttachImageUuidFile;
    }
}
