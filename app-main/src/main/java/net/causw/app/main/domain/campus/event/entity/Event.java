package net.causw.app.main.domain.campus.event.entity;

import org.hibernate.annotations.ColumnDefault;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.EventAttachImage;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_event")
public class Event extends BaseEntity {
	@Column(name = "url", nullable = false)
	private String url;

	@OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, mappedBy = "event")
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
