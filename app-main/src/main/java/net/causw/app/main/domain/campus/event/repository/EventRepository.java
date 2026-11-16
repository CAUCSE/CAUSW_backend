package net.causw.app.main.domain.campus.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.event.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

	Optional<Event> findByIdAndDeletedAtIsNull(String id);

	List<Event> findAllByIsActiveTrueAndDeletedAtIsNullOrderByDisplayOrderAscCreatedAtDesc();

	List<Event> findAllByDeletedAtIsNullOrderByDisplayOrderAscCreatedAtDesc();

	List<Event> findAllByIdInAndDeletedAtIsNull(List<String> ids);

	@Query("SELECT MAX(e.displayOrder) FROM Event e WHERE e.deletedAt IS NULL")
	Integer findMaxDisplayOrder();
}
