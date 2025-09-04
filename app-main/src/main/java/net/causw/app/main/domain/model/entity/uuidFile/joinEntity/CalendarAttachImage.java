package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import net.causw.app.main.domain.model.entity.calendar.Calendar;
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
@Table(name = "tb_calendar_attach_image_uuid_file",
	indexes = {
		@Index(name = "idx_calendar_attach_image_calendar_id", columnList = "calendar_id"),
		@Index(name = "idx_calendar_attach_image_uuid_file_id", columnList = "uuid_file_id")
	})
public class CalendarAttachImage extends JoinEntity {

	@Getter
	@Setter(AccessLevel.PUBLIC)
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
	public UuidFile uuidFile;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendar_id", nullable = false)
	private Calendar calendar;

	public static CalendarAttachImage of(Calendar calendar, UuidFile uuidFile) {
		return CalendarAttachImage.builder()
			.uuidFile(uuidFile)
			.calendar(calendar)
			.build();
	}

}
