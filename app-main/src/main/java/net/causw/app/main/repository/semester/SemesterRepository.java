package net.causw.app.main.repository.semester;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.model.entity.semester.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {
	List<Semester> findAllByIsCurrent(Boolean isCurrent);
}
