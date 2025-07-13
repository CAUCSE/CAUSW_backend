package net.causw.app.main.domain.model.entity.event;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.EventAttachImage;
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
