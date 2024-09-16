package net.causw.adapter.persistence.event;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.EventAttachImage;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_event")
public class Event extends BaseEntity {
    @Column(name = "url", nullable = false)
    private String url;

    @OneToOne(cascade = { CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "event")
    @JoinColumn(nullable = false)
    private EventAttachImage eventAttachImage;

    @Setter
    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    public static Event of(
            String url,
            UuidFile eventImageUuidFile,
            Boolean isDeleted
    ) {
        Event event = Event.builder()
                .url(url)
                .isDeleted(isDeleted)
                .build();

        EventAttachImage eventAttachImage = EventAttachImage.of(
                event,
                eventImageUuidFile
        );

        event.setEventImageUuidFile(eventAttachImage);

        return event;
    }

    public void update(String url, EventAttachImage eventAttachImage) {
        this.url = url;
        this.eventAttachImage = eventAttachImage;
    }

    private void setEventImageUuidFile(EventAttachImage eventAttachImage) {
        this.eventAttachImage = eventAttachImage;
    }
}
