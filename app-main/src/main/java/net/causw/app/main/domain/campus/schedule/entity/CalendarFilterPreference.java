package net.causw.app.main.domain.campus.schedule.entity;

import java.util.HashSet;
import java.util.Set;

import net.causw.app.main.domain.campus.schedule.enums.ScheduleCategory;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.entity.BaseEntity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_calendar_filter_preference")
public class CalendarFilterPreference extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@ElementCollection(fetch = FetchType.LAZY)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "tb_calendar_filter_category", joinColumns = @JoinColumn(name = "calendar_filter_preference_id"))
	@Column(name = "category", nullable = false)
	@Builder.Default
	private Set<ScheduleCategory> categories = new HashSet<>();

	public static CalendarFilterPreference of(User user) {
		return CalendarFilterPreference.builder()
			.user(user)
			.build();
	}

	public void updateCategories(Set<ScheduleCategory> categories) {
		this.categories.clear();

		if (categories != null) {
			this.categories.addAll(categories);
		}
	}
}