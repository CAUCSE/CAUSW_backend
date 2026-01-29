package net.causw.app.main.domain.campus.schedule.entity;

import java.time.LocalDateTime;

import net.causw.app.main.domain.campus.schedule.enums.ScheduleType;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "tb_schedule")
public class Schedule extends BaseEntity {
	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "type", nullable = false)
	private ScheduleType type;

	@Column(name = "start", nullable = false)
	private LocalDateTime start;

	@Column(name = "end", nullable = false)
	private LocalDateTime end;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator")
	private User creator;

	public static Schedule of(String title, ScheduleType type, LocalDateTime start, LocalDateTime end, User creator) {
		return Schedule.builder()
			.title(title)
			.type(type)
			.start(start)
			.end(end)
			.creator(creator)
			.build();
	}

	public void update(String title, ScheduleType type, LocalDateTime start, LocalDateTime end, User creator) {
		this.title = title;
		this.type = type;
		this.start = start;
		this.end = end;
		this.creator = creator;
	}

}
