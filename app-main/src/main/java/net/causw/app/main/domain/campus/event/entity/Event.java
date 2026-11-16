package net.causw.app.main.domain.campus.event.entity;

import java.time.LocalDateTime;

import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	public static final int DISPLAY_ORDER_INTERVAL = 10;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "landing_url", nullable = false, columnDefinition = "TEXT")
	private String landingUrl;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static Event of(String title, String landingUrl, Boolean isActive, Integer displayOrder) {
		return Event.builder()
			.title(title)
			.landingUrl(landingUrl)
			.isActive(isActive)
			.displayOrder(displayOrder)
			.build();
	}

	public void update(String title, String landingUrl, Boolean isActive) {
		this.title = title;
		this.landingUrl = landingUrl;
		this.isActive = isActive;
	}

	public void updateDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public void softDelete() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}
}
