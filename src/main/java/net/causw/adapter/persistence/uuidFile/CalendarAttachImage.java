package net.causw.adapter.persistence.uuidFile;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.calendar.Calendar;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_calendar_attach_image_uuid_file")
public class CalendarAttachImage extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false)
    private UuidFile uuidFile;

    public static CalendarAttachImage of(Calendar calendar, UuidFile uuidFile) {
        return CalendarAttachImage.builder()
            .calendar(calendar)
            .uuidFile(uuidFile)
            .build();
    }

    public CalendarAttachImage updateUuidFileAndReturnSelf(UuidFile uuidFile) {
        this.uuidFile = uuidFile;
        return this;
    }

}
