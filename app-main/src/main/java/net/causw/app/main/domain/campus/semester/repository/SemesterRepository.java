package net.causw.app.main.domain.campus.semester.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.causw.app.main.domain.campus.semester.entity.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {
	List<Semester> findAllByIsCurrent(Boolean isCurrent);
}
