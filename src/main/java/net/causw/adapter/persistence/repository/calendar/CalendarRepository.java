package net.causw.adapter.persistence.repository.calendar;

import net.causw.adapter.persistence.calendar.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, String> {

    List<Calendar> findByYearOrderByMonthDesc(int year);

    Optional<Calendar> findFirstByOrderByYearDescMonthDesc();

    Optional<Calendar> findByYearAndMonth(int year, int month);
}
