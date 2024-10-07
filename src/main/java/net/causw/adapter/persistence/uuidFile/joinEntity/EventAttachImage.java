package net.causw.adapter.persistence.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.event.Event;
import net.causw.adapter.persistence.uuidFile.UuidFile;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_event_attach_image_uuid_file",
indexes = {
    @Index(name = "idx_event_attach_image_event_id", columnList = "event_id"),
    @Index(name = "idx_event_attach_image_uuid_file_id", columnList = "uuid_file_id")
})
public class EventAttachImage extends JoinEntity {

    @Getter
    @Setter(AccessLevel.PUBLIC)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
    public UuidFile uuidFile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public static EventAttachImage of(Event event, UuidFile uuidFile) {
        return EventAttachImage.builder()
                .uuidFile(uuidFile)
                .event(event)
                .build();
    }

}
