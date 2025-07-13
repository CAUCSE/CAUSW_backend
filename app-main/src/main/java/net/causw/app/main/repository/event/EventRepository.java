package net.causw.app.main.repository.event;

import net.causw.app.main.domain.model.entity.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findByIsDeletedIsFalseOrderByCreatedAtDesc();
}
