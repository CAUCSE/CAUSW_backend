package net.causw.app.main.domain.moving.repository.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.moving.model.entity.event.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

	List<Event> findByIsDeletedIsFalseOrderByCreatedAtDesc();
}
