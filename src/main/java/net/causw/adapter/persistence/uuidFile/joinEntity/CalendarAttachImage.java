package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.calendar.Calendar;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_calendar_attach_image_uuid_file",
indexes = {
    @Index(name = "idx_calendar_attach_image_calendar_id", columnList = "calendar_id"),
    @Index(name = "idx_calendar_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class CalendarAttachImage extends JoinEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;

    private CalendarAttachImage(Calendar calendar, UuidFile uuidFile) {
        super(uuidFile);
        this.calendar = calendar;
    }

    public static CalendarAttachImage of(Calendar calendar, UuidFile uuidFile) {
        return new CalendarAttachImage(calendar, uuidFile);
    }

    public CalendarAttachImage updateUuidFileAndReturnSelf(UuidFile uuidFile) {
        this.uuidFile = uuidFile;
        return this;
    }

}
