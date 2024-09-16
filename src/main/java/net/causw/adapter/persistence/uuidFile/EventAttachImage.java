package net.causw.adapter.persistence.uuidFile;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.event.Event;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_event_attach_image_uuid_file")
public class EventAttachImage extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false)
    private UuidFile uuidFile;

    public static EventAttachImage of(Event event, UuidFile uuidFile) {
        return EventAttachImage.builder()
            .event(event)
            .uuidFile(uuidFile)
            .build();
    }

    public EventAttachImage updateUuidFileAndReturnSelf(UuidFile uuidFile) {
        this.uuidFile = uuidFile;
        return this;
    }
}
