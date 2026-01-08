package net.causw.app.main.domain.campus.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.event.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

	List<Event> findByIsDeletedIsFalseOrderByCreatedAtDesc();
}
