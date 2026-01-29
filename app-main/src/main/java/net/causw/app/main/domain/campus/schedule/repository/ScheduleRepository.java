package net.causw.app.main.domain.campus.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

	// 기간에 걸치는 Schedule 조회
	@Query("SELECT s FROM Schedule s WHERE s.start <= :end AND s.end >= :start")
	List<Schedule> findAllByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}