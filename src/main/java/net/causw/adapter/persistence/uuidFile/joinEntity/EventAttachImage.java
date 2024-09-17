package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.event.Event;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_event_attach_image_uuid_file",
indexes = {
    @Index(name = "idx_event_attach_image_event_id", columnList = "event_id"),
    @Index(name = "idx_event_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class EventAttachImage extends JoinEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private EventAttachImage(Event event, UuidFile uuidFile) {
        super(uuidFile);
        this.event = event;
    }

    public static EventAttachImage of(Event event, UuidFile uuidFile) {
        return new EventAttachImage(event, uuidFile);
    }

    public EventAttachImage updateUuidFileAndReturnSelf(UuidFile uuidFile) {
        this.uuidFile = uuidFile;
        return this;
    }

}
