package net.causw.app.main.repository.semester;

import net.causw.app.main.domain.model.entity.semester.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {
    List<Semester> findAllByIsCurrent(Boolean isCurrent);
}
