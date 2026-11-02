package net.causw.app.main.domain.campus.schedule.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.schedule.entity.CalendarFilterPreference;

@Repository
public interface CalendarFilterPreferenceRepository
	extends JpaRepository<CalendarFilterPreference, String> {

	Optional<CalendarFilterPreference> findByUserId(String userId);
}