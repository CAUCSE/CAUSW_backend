package net.causw.app.main.domain.campus.event.entity;

import java.time.LocalDateTime;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.EventAttachImage;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_event")
public class Event extends BaseEntity {

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "landing_url", columnDefinition = "TEXT")
	private String landingUrl;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;

	@OneToOne(mappedBy = "event", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private EventAttachImage eventAttachImage;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static Event of(String title, String landingUrl, Boolean isActive, Integer displayOrder, UuidFile image) {
		Event event = Event.builder()
			.title(title)
			.landingUrl(landingUrl)
			.isActive(isActive)
			.displayOrder(displayOrder)
			.build();
		event.eventAttachImage = EventAttachImage.of(event, image);
		return event;
	}

	public void update(String title, String landingUrl, Boolean isActive, Integer displayOrder) {
		this.title = title;
		this.landingUrl = landingUrl;
		this.isActive = isActive;
		this.displayOrder = displayOrder;
	}

	public void updateImage(UuidFile image) {
		this.eventAttachImage.setUuidFile(image);
	}

	public UuidFile getImage() {
		return this.eventAttachImage.getUuidFile();
	}

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}
}
