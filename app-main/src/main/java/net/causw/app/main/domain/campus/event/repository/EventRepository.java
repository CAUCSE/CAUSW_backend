package net.causw.app.main.domain.campus.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.event.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

	@EntityGraph(attributePaths = {"eventAttachImage", "eventAttachImage.uuidFile"})
	Optional<Event> findByIdAndDeletedAtIsNull(String id);

	@EntityGraph(attributePaths = {"eventAttachImage", "eventAttachImage.uuidFile"})
	List<Event> findAllByIsActiveTrueAndDeletedAtIsNullOrderByDisplayOrderAscCreatedAtDesc();

	@EntityGraph(attributePaths = {"eventAttachImage", "eventAttachImage.uuidFile"})
	List<Event> findAllByDeletedAtIsNullOrderByDisplayOrderAscCreatedAtDesc();
}
