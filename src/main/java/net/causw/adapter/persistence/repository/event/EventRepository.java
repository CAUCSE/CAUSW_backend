package net.causw.adapter.persistence.repository.event;

import net.causw.adapter.persistence.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findByIsDeletedIsFalseOrderByCreatedAtDesc();
}
