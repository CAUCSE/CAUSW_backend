package net.causw.app.main.domain.model.entity.calendar;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CalendarAttachImage;
import net.causw.app.main.domain.model.entity.base.BaseEntity;

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
