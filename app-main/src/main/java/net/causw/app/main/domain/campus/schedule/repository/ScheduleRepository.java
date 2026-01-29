package net.causw.app.main.domain.campus.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.schedule.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

}