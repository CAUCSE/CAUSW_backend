package net.causw.app.main.domain.model.entity.calendar;

import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.CalendarAttachImage;

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

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_calendar")
public class Calendar extends BaseEntity {
	@Column(name = "year", nullable = false)
	private Integer year;

	@Column(name = "month", nullable = false)
	private Integer month;

	@OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, mappedBy = "calendar")
	@JoinColumn(nullable = false)
	private CalendarAttachImage calendarAttachImage;

	public static Calendar of(
		Integer year,
		Integer month,
		UuidFile uuidFile
	) {
		Calendar calendar = Calendar.builder()
			.year(year)
			.month(month)
			.build();

		CalendarAttachImage calendarAttachImage = CalendarAttachImage.of(
			calendar,
			uuidFile
		);

		calendar.setCalendarAttachImage(calendarAttachImage);

		return calendar;
	}

	public void update(Integer year, Integer month, CalendarAttachImage calendarAttachImage) {
		this.year = year;
		this.month = month;
		this.calendarAttachImage = calendarAttachImage;
	}

	private void setCalendarAttachImage(CalendarAttachImage calendarAttachImage) {
		this.calendarAttachImage = calendarAttachImage;
	}
}
