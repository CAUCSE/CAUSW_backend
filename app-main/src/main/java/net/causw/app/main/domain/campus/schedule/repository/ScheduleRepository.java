package net.causw.app.main.domain.campus.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;
import net.causw.app.main.domain.campus.schedule.entity.enums.ScheduleType;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

	// 기간에 걸치는 Schedule 조회 (여러 타입 지원)
	@Query("SELECT s FROM Schedule s WHERE s.start <= :to AND s.end >= :from AND (:types IS NULL OR s.type IN :types)")
	List<Schedule> findAllByCondition(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
		@Param("types") List<ScheduleType> types);
}