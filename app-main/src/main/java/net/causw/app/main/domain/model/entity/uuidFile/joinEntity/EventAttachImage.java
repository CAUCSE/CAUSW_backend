package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.event.Event;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
