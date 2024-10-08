package net.causw.adapter.persistence.calendar;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.joinEntity.CalendarAttachImage;
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

    @OneToOne(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "calendar")
    @JoinColumn(nullable = false)
    private CalendarAttachImage calendarAttachImage;

    public static Calendar of(
            Integer year,
            Integer month,
            UuidFile uuidFile
    ) {
        Calendar calendar = Calendar.builder()
                .year(year)
                .month(month)
                .build();

        CalendarAttachImage calendarAttachImage = CalendarAttachImage.of(
                calendar,
                uuidFile
        );

        calendar.setCalendarAttachImage(calendarAttachImage);

        return calendar;
    }

    public void update(Integer year, Integer month, CalendarAttachImage calendarAttachImage) {
        this.year = year;
        this.month = month;
        this.calendarAttachImage = calendarAttachImage;
    }

    private void setCalendarAttachImage(CalendarAttachImage calendarAttachImage) {
        this.calendarAttachImage = calendarAttachImage;
    }
}
