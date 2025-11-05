package net.causw.app.main.domain.moving.repository.semester;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.moving.model.entity.semester.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {
	List<Semester> findAllByIsCurrent(Boolean isCurrent);
}
